package com.climacast.batch_server.config.manager.impl

import com.climacast.batch_server.common.enums.WeatherStatus
import com.climacast.batch_server.config.manager.WeatherSaveManager
import com.climacast.batch_server.dto.HourlyWeatherUpsertRequestDTO
import com.climacast.batch_server.dto.WeatherResponseDTO
import com.climacast.batch_server.model.entity.DailyWeather
import com.climacast.batch_server.model.entity.DailyWeatherData
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
        val hourlyWeatherUpsertDTOs = linkedSetOf<HourlyWeatherUpsertRequestDTO>()
        val dailyWeathers = linkedSetOf<DailyWeather>()

        weathers.forEach { weather ->
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
            } ?:
            weather.daily?.let {
                it.time.forEachIndexed { index, time ->
                    val weatherCode = it.weather_code!![index]
                    val dailyWeather = DailyWeather(
                        weather.parentRegion!!,
                        weather.childRegion!!,
                        weather.latitude,
                        weather.longitude,
                        WeatherStatus.of(weatherCode),
                        LocalDate.parse(time).atStartOfDay(),
                        DailyWeatherData(
                            weatherCode,
                            it.temperature_2m_max?.getOrNull(index),
                            it.temperature_2m_min?.getOrNull(index),
                            parseLocalDateTime(it.sunrise, index),
                            parseLocalDateTime(it.sunset, index),
                            it.precipitation_sum?.getOrNull(index)
                        )
                    )
                    dailyWeathers.add(dailyWeather)
                }
            }
        }

        when {
            hourlyWeatherUpsertDTOs.isNotEmpty() -> hourlyWeatherRepository.upsertHourlyWeatherForecasts(hourlyWeatherUpsertDTOs)
            dailyWeathers.isNotEmpty() -> dailyWeatherRepository.saveAll(dailyWeathers)
        }
    }

    companion object {
        private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

        fun parseLocalDateTime(list: List<String>?, idx: Int): LocalDateTime? =
            list?.let { l ->
                l.getOrNull(idx)?.let {
                    LocalDateTime.parse(it, DATETIME_FORMATTER)
                }
            }
    }

    override fun saveOnElasticsearch(weathers: List<WeatherResponseDTO>) {
        TODO("Not yet implemented")
    }
}