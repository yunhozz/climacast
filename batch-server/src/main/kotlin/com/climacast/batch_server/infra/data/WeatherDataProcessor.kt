package com.climacast.batch_server.infra.data

import com.climacast.global.dto.WeatherResponseDTO

interface WeatherDataProcessor {
    fun process(weatherResponseDTOs: List<WeatherResponseDTO>)
}