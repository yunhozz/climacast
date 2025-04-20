package com.climacast.subscription_service.infra.document.save

import com.climacast.global.dto.WeatherResponseDTO
import com.climacast.global.enums.WeatherStatus
import com.climacast.global.event.KafkaMessage
import com.climacast.global.event.message.ForecastWeathersMessage
import com.climacast.global.event.message.HistoryWeathersMessage
import com.climacast.global.utils.DateTimeConverter
import com.climacast.global.utils.logger
import com.climacast.subscription_service.model.document.ForecastWeather
import com.climacast.subscription_service.model.document.HistoryWeather
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class AbstractDocumentSaver : DocumentSaver {

    protected val log = logger()

    abstract fun saveForecastWeathers(forecastWeathers: List<ForecastWeather>)
    abstract fun saveHistoryWeathers(historyWeathers: List<HistoryWeather>)

    override fun saveWeathersByMessageType(message: KafkaMessage) {
        when (message) {
            is ForecastWeathersMessage -> {
                makeForecastWeathers(message.data)
                    .subscribe { saveForecastWeathers(it) }
            }
            is HistoryWeathersMessage -> {
                makeHistoryWeathers(message.data)
                    .subscribe { saveHistoryWeathers(it) }
            }
            else -> {
                log.warn("Unsupported message type: ${message::class.simpleName}")
            }
        }
    }

    private fun makeForecastWeathers(data: List<WeatherResponseDTO>): Mono<List<ForecastWeather>> =
        Flux.fromIterable(data)
            .map { weather ->
                ForecastWeather(
                    lat = weather.latitude,
                    lon = weather.longitude,
                    region = "${weather.parentRegion} ${weather.childRegion}",
                    time = weather.hourly?.time?.map { DateTimeConverter.convertToLocalDateTime(it).toString() },
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

    private fun makeHistoryWeathers(data: List<WeatherResponseDTO>): Mono<List<HistoryWeather>> =
        Flux.fromIterable(data)
            .map { weather ->
                HistoryWeather(
                    lat = weather.latitude,
                    lon = weather.longitude,
                    region = "${weather.parentRegion} ${weather.childRegion}",
                    time = weather.hourly?.time?.map { DateTimeConverter.convertToLocalDateTime(it).toString() },
                    weatherStatus = weather.daily?.let {
                        it.weather_code?.map { code -> WeatherStatus.of(code).name }
                    } ?: weather.hourly?.weather_code?.map { code -> WeatherStatus.of(code).name },
                    maxTemperature2m = weather.daily?.temperature_2m_max,
                    minTemperature2m = weather.daily?.temperature_2m_min,
                    maxApparentTemperature = weather.daily?.apparent_temperature_max,
                    minApparentTemperature = weather.daily?.apparent_temperature_min,
                    sunrise = weather.daily?.sunrise?.map { DateTimeConverter.convertToLocalDateTime(it).toString() },
                    sunset = weather.daily?.sunset?.map { DateTimeConverter.convertToLocalDateTime(it).toString() },
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
}