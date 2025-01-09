package com.climacast.batch_server.service

import com.climacast.batch_server.config.handler.data.AbstractWeatherDataHandler
import com.climacast.batch_server.config.handler.data.ConvertedWeatherData
import com.climacast.batch_server.model.repository.DailyWeatherRepository
import com.climacast.batch_server.model.repository.HourlyWeatherRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WeatherDataService(
    private val dailyWeatherRepository: DailyWeatherRepository,
    private val hourlyWeatherRepository: HourlyWeatherRepository
) : AbstractWeatherDataHandler() {

    @Transactional
    override fun saveWeatherForecastData(weatherForecastData: ConvertedWeatherData.WeatherForecastData) {
        hourlyWeatherRepository.upsertHourlyWeatherForecasts(weatherForecastData.hourlyWeatherUpsertDTOs)
    }

    @Transactional
    override fun saveWeatherHistoryData(weatherHistoryData: ConvertedWeatherData.WeatherHistoryData) {
        dailyWeatherRepository.saveAll(weatherHistoryData.dailyWeathers)
        hourlyWeatherRepository.saveAll(weatherHistoryData.hourlyWeathers)
    }
}