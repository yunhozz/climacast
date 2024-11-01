package com.climacast.batch_server.config.manager

import com.climacast.batch_server.dto.WeatherResponseDTO
import reactor.core.publisher.Mono

interface OpenApiManager {
    fun callWeatherInfoByMySite(city: String): Mono<WeatherResponseDTO>
    fun saveWeatherHistoryOneDayAgo()
}