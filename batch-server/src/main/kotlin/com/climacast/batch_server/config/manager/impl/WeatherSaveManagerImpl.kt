package com.climacast.batch_server.config.manager.impl

import com.climacast.batch_server.common.enums.WeatherStatus
import com.climacast.batch_server.common.enums.WeatherType
import com.climacast.batch_server.config.manager.WeatherSaveManager
import com.climacast.batch_server.dto.HourlyWeatherUpsertRequestDTO
import com.climacast.batch_server.dto.WeatherResponseDTO
import com.climacast.batch_server.model.entity.DailyWeather
import com.climacast.batch_server.model.entity.DailyWeatherData
import com.climacast.batch_server.model.entity.HourlyWeather
import com.climacast.batch_server.model.entity.HourlyWeatherData
import com.climacast.batch_server.model.repository.DailyWeatherRepository
import com.climacast.batch_server.model.repository.HourlyWeatherRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class WeatherSaveManagerImpl(
    private val dailyWeatherRepository: DailyWeatherRepository,
    private val hourlyWeatherRepository: HourlyWeatherRepository
): WeatherSaveManager {

    @Transactional
    override fun saveOnMysql(weathers: List<WeatherResponseDTO>) {
        weathers.forEach { weather ->
            when (weather.weatherType!!) {
                WeatherType.FORECAST -> saveWeatherForecastDataInJDBC(weather)
                WeatherType.HISTORY -> saveWeatherHistoryDataInJPA(weather)
            }
        }
    }

    private fun saveWeatherForecastDataInJDBC(weather: WeatherResponseDTO) {
        val hourlyWeatherUpsertDTOs = linkedSetOf<HourlyWeatherUpsertRequestDTO>()
        weather.hourly?.let {
            it.time.forEachIndexed { index, time ->
                val weatherCode = it.weather_code!![index]
                val hourlyWeatherUpsertDTO = HourlyWeatherUpsertRequestDTO(
                    weather.parentRegion!!,
                    weather.childRegion!!,
                    weather.latitude,
                    weather.longitude,
                    WeatherStatus.of(weatherCode).name,
                    LocalDateTime.parse(time),
                    weatherCode,
                    it.temperature_2m?.getOrNull(index),
                    it.temperature_80m?.getOrNull(index),
                    it.temperature_120m?.getOrNull(index),
                    it.temperature_180m?.getOrNull(index),
                    it.wind_speed_10m?.getOrNull(index),
                    it.wind_speed_80m?.getOrNull(index),
                    it.wind_speed_120m?.getOrNull(index),
                    it.wind_speed_180m?.getOrNull(index),
                    it.relative_humidity_2m?.getOrNull(index)
                )
                hourlyWeatherUpsertDTOs.add(hourlyWeatherUpsertDTO)
            }
        }

        hourlyWeatherRepository.upsertHourlyWeatherForecasts(hourlyWeatherUpsertDTOs)
    }

    private fun saveWeatherHistoryDataInJPA(weather: WeatherResponseDTO) {
        val dailyWeathers = linkedSetOf<DailyWeather>()
        val hourlyWeathers = linkedSetOf<HourlyWeather>()
        weather.daily?.let {
            it.time.first { time ->
                val weatherCode = it.weather_code!!.first()
                val dailyWeather = DailyWeather(
                    weather.parentRegion!!,
                    weather.childRegion!!,
                    weather.latitude,
                    weather.longitude,
                    WeatherStatus.of(weatherCode),
                    LocalDate.parse(time),
                    DailyWeatherData(
                        weatherCode,
                        it.temperature_2m_max?.firstOrNull(),
                        it.temperature_2m_min?.firstOrNull(),
                        it.apparent_temperature_max?.firstOrNull(),
                        it.apparent_temperature_min?.firstOrNull(),
                        parseLocalDateTime(it.sunrise),
                        parseLocalDateTime(it.sunset),
                        it.daylight_duration?.firstOrNull(),
                        it.sunshine_duration?.firstOrNull(),
                        it.uv_index_max?.firstOrNull(),
                        it.uv_index_clear_sky_max?.firstOrNull(),
                        it.precipitation_sum?.firstOrNull(),
                        it.rain_sum?.firstOrNull(),
                        it.showers_sum?.firstOrNull(),
                        it.snowfall_sum?.firstOrNull(),
                        it.precipitation_hours?.firstOrNull(),
                        it.precipitation_probability_max?.firstOrNull(),
                        it.wind_speed_10m_max?.firstOrNull(),
                        it.wind_gusts_10m_max?.firstOrNull(),
                        it.wind_direction_10m_dominant?.firstOrNull(),
                        it.shortwave_radiation_sum?.firstOrNull(),
                        it.et0_fao_evapotranspiration?.firstOrNull()
                    )
                )
                dailyWeathers.add(dailyWeather)
            }
        }

        weather.hourly?.let {
            it.time.forEachIndexed { index, time ->
                val weatherCode = it.weather_code!![index]
                val hourlyWeather = HourlyWeather(
                    weather.parentRegion!!,
                    weather.childRegion!!,
                    weather.latitude,
                    weather.longitude,
                    WeatherStatus.of(weatherCode),
                    LocalDateTime.parse(time),
                    HourlyWeatherData(
                        weatherCode,
                        it.temperature_2m?.getOrNull(index),
                        it.temperature_80m?.getOrNull(index),
                        it.temperature_120m?.getOrNull(index),
                        it.temperature_180m?.getOrNull(index),
                        it.wind_speed_10m?.getOrNull(index),
                        it.wind_speed_80m?.getOrNull(index),
                        it.wind_speed_120m?.getOrNull(index),
                        it.wind_speed_180m?.getOrNull(index),
                        it.relative_humidity_2m?.getOrNull(index)
                    )
                )
                hourlyWeathers.add(hourlyWeather)
            }
        }

        dailyWeatherRepository.saveAll(dailyWeathers)
        hourlyWeatherRepository.saveAll(hourlyWeathers)
    }

    companion object {
        private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

        fun parseLocalDateTime(list: List<String>?): LocalDateTime? =
            list?.let { l ->
                l.firstOrNull()?.let {
                    LocalDateTime.parse(it, DATETIME_FORMATTER)
                }
            }
    }

    override fun saveOnElasticsearch(weathers: List<WeatherResponseDTO>) {
        TODO("Not yet implemented")
    }
}