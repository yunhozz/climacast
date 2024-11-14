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
                        it.temperature_2m!![index],
                        it.temperature_80m!![index],
                        it.temperature_120m!![index],
                        it.temperature_180m!![index],
                        it.wind_speed_10m!![index],
                        it.wind_speed_80m!![index],
                        it.wind_speed_120m!![index],
                        it.wind_speed_180m!![index],
                        it.relative_humidity_2m!![index]
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
                            it.temperature_2m_max!![index],
                            it.temperature_2m_min!![index],
                            LocalDateTime.parse(it.sunrise!![index], DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                            LocalDateTime.parse(it.sunset!![index], DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                            it.precipitation_sum!![index]
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

    override fun saveOnElasticsearch(weathers: List<WeatherResponseDTO>) {
        TODO("Not yet implemented")
    }
}