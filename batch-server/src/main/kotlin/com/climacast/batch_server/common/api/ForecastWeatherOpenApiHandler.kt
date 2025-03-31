package com.climacast.batch_server.common.api

import com.climacast.batch_server.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.dto.Region
import com.climacast.global.dto.WeatherResponseDTO
import com.climacast.global.enums.WeatherType
import org.springframework.stereotype.Component

@Component
class ForecastWeatherOpenApiHandler(
    private val weatherResponseProcessor: WeatherResponseProcessor
) : OpenApiHandler<Region, WeatherResponseDTO>() {

    override fun callOpenApi(): List<WeatherResponseDTO> {
        val (hourlyValues, _, _, forecastDays) = query as OpenApiQueryRequestDTO
        val hourly = hourlyValues?.joinToString(",")
        val req = WeatherQueryRequest(pastDays = 0, forecastDays, weatherType = WeatherType.FORECAST)

        return callWeatherOpenApi { region ->
            weatherResponseProcessor.sendHourlyWeatherRequest(region, req, hourly)
        } ?: throw IllegalArgumentException("There are no forecast weather data")
    }
}