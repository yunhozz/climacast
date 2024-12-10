package com.climacast.batch_server.config.handler

import com.climacast.batch_server.dto.HourlyWeatherUpsertRequestDTO
import com.climacast.batch_server.model.entity.DailyWeather
import com.climacast.batch_server.model.entity.HourlyWeather
import com.climacast.global.dto.WeatherResponseDTO

interface WeatherDataHandler {
    fun process(weatherResponseDTOs: List<WeatherResponseDTO>) {
        weatherResponseDTOs.forEach { dto ->
            val weatherData = convertToPojo(dto)
            saveWeatherData(weatherData)
        }
    }

    fun convertToPojo(dto: WeatherResponseDTO): ConvertedWeatherData
    fun saveWeatherData(weatherData: ConvertedWeatherData)
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