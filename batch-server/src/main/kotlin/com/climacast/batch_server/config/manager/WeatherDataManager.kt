package com.climacast.batch_server.config.manager

import com.climacast.batch_server.dto.HourlyWeatherUpsertRequestDTO
import com.climacast.batch_server.dto.WeatherResponseDTO
import com.climacast.batch_server.model.entity.DailyWeather
import com.climacast.batch_server.model.entity.HourlyWeather

interface WeatherDataManager {
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
    data class WeatherForecastData(val hourlyWeatherUpsertDTOs: Set<HourlyWeatherUpsertRequestDTO>): ConvertedWeatherData()
    data class WeatherHistoryData(val dailyWeathers: Set<DailyWeather>, val hourlyWeathers: Set<HourlyWeather>): ConvertedWeatherData()
}