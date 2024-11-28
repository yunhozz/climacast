package com.climacast.batch_server.config.handler

import com.climacast.batch_server.dto.Region
import com.climacast.batch_server.dto.WeatherResponseDTO
import reactor.core.publisher.Mono

interface WeatherResponseParser {
    fun sendHourlyWeatherRequest(region: Region, query: WeatherQueryRequest, hourly: String?): Mono<WeatherResponseDTO>
    fun sendDailyWeatherRequest(region: Region, query: WeatherQueryRequest, daily: String?): Mono<WeatherResponseDTO>
}