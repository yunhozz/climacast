package com.climacast.batch_server.config.handler

import com.climacast.batch_server.common.enums.WeatherParameters
import com.climacast.global.dto.WeatherResponseDTO

interface KafkaMessageHandler {
    fun sendWeatherResponses(param: WeatherParameters, weatherData: List<WeatherResponseDTO>)
}