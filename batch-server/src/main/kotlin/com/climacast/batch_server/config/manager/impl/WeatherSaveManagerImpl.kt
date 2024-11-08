package com.climacast.batch_server.config.manager.impl

import com.climacast.batch_server.common.enums.WeatherStatus
import com.climacast.batch_server.config.manager.WeatherSaveManager
import com.climacast.batch_server.dto.WeatherResponseDTO
import com.climacast.batch_server.model.entity.DailyWeather
import com.climacast.batch_server.model.entity.DailyWeatherData
import com.climacast.batch_server.model.entity.HourlyWeather
import com.climacast.batch_server.model.entity.HourlyWeatherData
import com.climacast.batch_server.model.repository.DailyWeatherRepository
import com.climacast.batch_server.model.repository.HourlyWeatherRepository
import com.climacast.batch_server.model.repository.WeatherDocumentRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class WeatherSaveManagerImpl(
    private val dailyWeatherRepository: DailyWeatherRepository,
    private val hourlyWeatherRepository: HourlyWeatherRepository,
    private val weatherDocumentRepository: WeatherDocumentRepository
): WeatherSaveManager {

    @Transactional
    override fun saveOnMysql(weathers: List<WeatherResponseDTO>) {
        println("weathers = ${weathers}")
        weathers.forEach { weather ->
            if (weather.hourly != null) {
                saveHourlyWeatherOnMysql(weather)
            } else {
                saveDailyWeatherOnMysql(weather)
            }
        }
    }

    private fun saveHourlyWeatherOnMysql(weather: WeatherResponseDTO) {
        val hourlyWeathers = arrayListOf<HourlyWeather>()
        val hourly = weather.hourly

        hourly!!.time.forEachIndexed { index, time ->
            val weatherCode = hourly.weather_code!![index]
            val hourlyWeather = HourlyWeather(
                weather.parentRegion!!,
                weather.childRegion!!,
                weather.latitude,
                weather.longitude,
                WeatherStatus.of(weatherCode),
                LocalDateTime.parse(time),
                HourlyWeatherData(
                    weatherCode,
                    hourly.temperature_2m!![index],
                    hourly.temperature_80m!![index],
                    hourly.temperature_120m!![index],
                    hourly.temperature_180m!![index],
                    hourly.wind_speed_10m!![index],
                    hourly.wind_speed_80m!![index],
                    hourly.wind_speed_120m!![index],
                    hourly.wind_speed_180m!![index],
                    hourly.relative_humidity_2m!![index]
                )
            )
            hourlyWeathers.add(hourlyWeather)
        }

        hourlyWeatherRepository.saveAll(hourlyWeathers)
    }

    private fun saveDailyWeatherOnMysql(weather: WeatherResponseDTO) {
        val dailyWeathers = arrayListOf<DailyWeather>()
        val daily = weather.daily

        daily!!.time.forEachIndexed { index, time ->
            println("weather.parentRegion = ${weather.parentRegion}")
            println("weather.childRegion = ${weather.childRegion}")
            val weatherCode = daily.weather_code!![index]
            val dailyWeather = DailyWeather(
                weather.parentRegion!!,
                weather.childRegion!!,
                weather.latitude,
                weather.longitude,
                WeatherStatus.of(weatherCode),
                LocalDate.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                DailyWeatherData(
                    weatherCode,
                    daily.temperature_2m_max!![index],
                    daily.temperature_2m_min!![index],
                    LocalDateTime.parse(daily.sunrise!![index], DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                    LocalDateTime.parse(daily.sunset!![index], DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                    daily.precipitation_sum!![index]
                )
            )
            dailyWeathers.add(dailyWeather)
        }

        dailyWeatherRepository.saveAll(dailyWeathers)
    }

    override fun saveOnElasticsearch(weathers: List<WeatherResponseDTO>) {
        TODO("Not yet implemented")
    }
}