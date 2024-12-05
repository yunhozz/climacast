package com.climacast.subscription_service.service

import com.climacast.global.dto.KafkaMessage
import com.climacast.global.enums.WeatherStatus
import com.climacast.global.utils.logger
import com.climacast.subscription_service.model.document.ForecastWeather
import com.climacast.subscription_service.model.document.HistoryWeather
import com.climacast.subscription_service.model.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.model.repository.HistoryWeatherSearchRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class WeatherStorageService(
    private val forecastWeatherSearchRepository: ForecastWeatherSearchRepository,
    private val historyWeatherSearchRepository: HistoryWeatherSearchRepository,
    private val reactiveKafkaConsumer: ReactiveKafkaConsumerTemplate<String, KafkaMessage>
) {
    private val log = logger()

    @EventListener(ApplicationReadyEvent::class)
    fun saveWeathers() {
        reactiveKafkaConsumer.receiveAutoAck()
            .doOnNext {
                log.info("Received from topic={}, offset={}", it.topic(), it.offset())
            }
            .map { it.value() }
            .flatMap { message ->
                when (message) {
                    is KafkaMessage.ForecastWeathersMessage -> {
                        Flux.fromIterable(message.data)
                            .map { weather ->
                                ForecastWeather(
                                    lat = weather.latitude,
                                    lon = weather.longitude,
                                    region = weather.parentRegion + " " + weather.childRegion,
                                    time = weather.hourly?.time,
                                    weatherStatus = weather.hourly?.weather_code?.map { code -> WeatherStatus.of(code).name },
                                    temperature2m = weather.hourly?.temperature_2m,
                                    temperature80m = weather.hourly?.temperature_80m,
                                    temperature120m = weather.hourly?.temperature_120m,
                                    temperature180m = weather.hourly?.temperature_180m,
                                    windSpeed10m = weather.hourly?.wind_speed_10m,
                                    windSpeed80m = weather.hourly?.wind_speed_80m,
                                    windSpeed120m = weather.hourly?.wind_speed_120m,
                                    windSpeed180m = weather.hourly?.wind_speed_180m,
                                    humidity2m = weather.hourly?.relative_humidity_2m
                                )
                            }
                            .collectList()
                            .flatMapMany { forecastWeathers ->
                                forecastWeatherSearchRepository.saveAll(forecastWeathers)
                            }
                    }
                    is KafkaMessage.HistoryWeathersMessage -> {
                        Flux.fromIterable(message.data)
                            .map { weather ->
                                HistoryWeather(
                                    lat = weather.latitude,
                                    lon = weather.longitude,
                                    region = weather.parentRegion + " " + weather.childRegion,
                                    time = weather.hourly?.time,
                                    weatherStatus = weather.daily?.let {
                                        it.weather_code?.map { code -> WeatherStatus.of(code).name }
                                    } ?: weather.hourly?.weather_code?.map { code -> WeatherStatus.of(code).name },
                                    maxTemperature2m = weather.daily?.temperature_2m_max,
                                    minTemperature2m = weather.daily?.temperature_2m_min,
                                    maxApparentTemperature = weather.daily?.apparent_temperature_max,
                                    minApparentTemperature = weather.daily?.apparent_temperature_min,
                                    sunrise = weather.daily?.sunrise,
                                    sunset = weather.daily?.sunset,
                                    daylightDuration = weather.daily?.daylight_duration,
                                    sunshineDuration = weather.daily?.sunshine_duration,
                                    maxUvIndex = weather.daily?.uv_index_max,
                                    maxUvIndexClearSky = weather.daily?.uv_index_clear_sky_max,
                                    precipitationSum = weather.daily?.precipitation_sum,
                                    rainSum = weather.daily?.rain_sum,
                                    showersSum = weather.daily?.showers_sum,
                                    snowfallSum = weather.daily?.snowfall_sum,
                                    precipitationHours = weather.daily?.precipitation_hours,
                                    maxPrecipitationProbability = weather.daily?.precipitation_probability_max,
                                    maxWindSpeed10m = weather.daily?.wind_speed_10m_max,
                                    maxWindGusts10m = weather.daily?.wind_gusts_10m_max,
                                    windDirection10m = weather.daily?.wind_direction_10m_dominant,
                                    sumOfShortwaveRadiation = weather.daily?.shortwave_radiation_sum,
                                    refEvapotranspiration = weather.daily?.et0_fao_evapotranspiration,
                                    temperature2m = weather.hourly?.temperature_2m,
                                    temperature80m = weather.hourly?.temperature_80m,
                                    temperature120m = weather.hourly?.temperature_120m,
                                    temperature180m = weather.hourly?.temperature_180m,
                                    windSpeed10m = weather.hourly?.wind_speed_10m,
                                    windSpeed80m = weather.hourly?.wind_speed_80m,
                                    windSpeed120m = weather.hourly?.wind_speed_120m,
                                    windSpeed180m = weather.hourly?.wind_speed_180m,
                                    humidity2m = weather.hourly?.relative_humidity_2m
                                )
                            }
                            .collectList()
                            .flatMapMany { historyWeathers ->
                                historyWeatherSearchRepository.saveAll(historyWeathers)
                            }
                    }
                }
            }
            .doOnError { log.error(it.localizedMessage, it) }
            .subscribe()
    }
}