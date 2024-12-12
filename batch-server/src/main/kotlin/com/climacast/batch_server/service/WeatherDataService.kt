package com.climacast.batch_server.service

import com.climacast.batch_server.config.handler.data.AbstractWeatherDataHandler
import com.climacast.batch_server.config.handler.data.ConvertedWeatherData
import com.climacast.batch_server.model.repository.DailyWeatherRepository
import com.climacast.batch_server.model.repository.HourlyWeatherRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class WeatherDataService(
    private val dailyWeatherRepository: DailyWeatherRepository,
    private val hourlyWeatherRepository: HourlyWeatherRepository
): AbstractWeatherDataHandler() {

    @Transactional
    override fun saveWeatherData(weatherData: ConvertedWeatherData) {
        when (weatherData) {
            is ConvertedWeatherData.WeatherForecastData ->
                hourlyWeatherRepository.upsertHourlyWeatherForecasts(weatherData.hourlyWeatherUpsertDTOs)
            is ConvertedWeatherData.WeatherHistoryData -> {
                dailyWeatherRepository.saveAll(weatherData.dailyWeathers)
                hourlyWeatherRepository.saveAll(weatherData.hourlyWeathers)
            }
        }
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
}