package com.climacast.batch_server.config.handler.data

import com.climacast.batch_server.dto.HourlyWeatherUpsertRequestDTO
import com.climacast.batch_server.model.entity.DailyWeather
import com.climacast.batch_server.model.entity.DailyWeatherData
import com.climacast.batch_server.model.entity.HourlyWeather
import com.climacast.batch_server.model.entity.HourlyWeatherData
import com.climacast.batch_server.service.WeatherDataService.Companion.parseLocalDateTime
import com.climacast.global.dto.WeatherResponseDTO
import com.climacast.global.enums.WeatherStatus
import com.climacast.global.enums.WeatherType
import java.time.LocalDate
import java.time.LocalDateTime

abstract class AbstractWeatherDataHandler: WeatherDataHandler {
    override fun process(weatherResponseDTOs: List<WeatherResponseDTO>) {
        weatherResponseDTOs.forEach { dto ->
            when (val weatherData = convertToPojo(dto)) {
                is ConvertedWeatherData.WeatherForecastData -> saveWeatherForecastData(weatherData)
                is ConvertedWeatherData.WeatherHistoryData -> saveWeatherHistoryData(weatherData)
            }
        }
    }

    abstract fun saveWeatherForecastData(weatherForecastData: ConvertedWeatherData.WeatherForecastData)

    abstract fun saveWeatherHistoryData(weatherHistoryData: ConvertedWeatherData.WeatherHistoryData)

    private fun convertToPojo(dto: WeatherResponseDTO): ConvertedWeatherData =
        when (dto.weatherType!!) {
            WeatherType.FORECAST -> {
                val hourlyWeatherUpsertDTOs = linkedSetOf<HourlyWeatherUpsertRequestDTO>()
                dto.hourly?.let {
                    it.time.forEachIndexed { index, time ->
                        val weatherCode = it.weather_code!![index]
                        val hourlyWeatherUpsertDTO = HourlyWeatherUpsertRequestDTO(
                            dto.parentRegion!!,
                            dto.childRegion!!,
                            dto.latitude,
                            dto.longitude,
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
                ConvertedWeatherData.WeatherForecastData(hourlyWeatherUpsertDTOs)
            }

            WeatherType.HISTORY -> {
                val dailyWeathers = linkedSetOf<DailyWeather>()
                val hourlyWeathers = linkedSetOf<HourlyWeather>()

                dto.daily?.let {
                    it.time.first { time ->
                        val weatherCode = it.weather_code!!.first()
                        val dailyWeather = DailyWeather(
                            dto.parentRegion!!,
                            dto.childRegion!!,
                            dto.latitude,
                            dto.longitude,
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

                dto.hourly?.let {
                    it.time.forEachIndexed { index, time ->
                        val weatherCode = it.weather_code!![index]
                        val hourlyWeather = HourlyWeather(
                            dto.parentRegion!!,
                            dto.childRegion!!,
                            dto.latitude,
                            dto.longitude,
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

                ConvertedWeatherData.WeatherHistoryData(dailyWeathers, hourlyWeathers)
            }
        }
}

sealed class ConvertedWeatherData {
    data class WeatherForecastData(
        val hourlyWeatherUpsertDTOs: Set<HourlyWeatherUpsertRequestDTO>
    ): ConvertedWeatherData()

    data class WeatherHistoryData(
        val dailyWeathers: Set<DailyWeather>,
        val hourlyWeathers: Set<HourlyWeather>
    ): ConvertedWeatherData()
}