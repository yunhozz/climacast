package com.climacast.batch_server.config.manager

import com.climacast.batch_server.config.Region
import com.climacast.batch_server.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.dto.WeatherResponseDTO

interface OpenApiManager {
    fun callForecastWeatherOpenApi(regions: Set<Region>, dto: OpenApiQueryRequestDTO): MutableList<WeatherResponseDTO>?
    fun callHistoricalWeatherOpenApi(regions: Set<Region>, dto: OpenApiQueryRequestDTO): MutableList<WeatherResponseDTO>?
}