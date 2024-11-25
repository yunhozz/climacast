package com.climacast.batch_server.config.manager

import com.climacast.batch_server.dto.WeatherResponseDTO

interface WeatherSaveManager {
    fun saveWeatherForecastDataInJDBC(weather: WeatherResponseDTO)
    fun saveWeatherHistoryDataInJPA(weather: WeatherResponseDTO)
}