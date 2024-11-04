package com.climacast.batch_server.config.manager

import com.climacast.batch_server.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.dto.WeatherResponseDTO

interface OpenApiManager {
    fun callForecastWeatherOpenApi(cityInfoList: List<String>, dto: OpenApiQueryRequestDTO): MutableList<WeatherResponseDTO>?
    fun callHistoricalWeatherOpenApi(cityInfoList: List<String>, dto: OpenApiQueryRequestDTO): MutableList<WeatherResponseDTO>?
}