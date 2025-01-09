package com.climacast.batch_server.config.handler.data

import com.climacast.global.dto.WeatherResponseDTO

interface WeatherDataHandler {
    fun process(weatherResponseDTOs: List<WeatherResponseDTO>)
}