package com.climacast.batch_server.config.handler

import com.climacast.batch_server.dto.Region
import com.climacast.global.dto.WeatherResponseDTO
import reactor.core.publisher.Mono

interface WeatherResponseProcessor {
    fun sendHourlyWeatherRequest(region: Region, query: WeatherQueryRequest, hourly: String?): Mono<WeatherResponseDTO>
    fun sendDailyWeatherRequest(region: Region, query: WeatherQueryRequest, daily: String?): Mono<WeatherResponseDTO>
}