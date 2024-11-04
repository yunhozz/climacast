package com.climacast.batch_server.dto

data class OpenApiQueryRequestDTO(
    val hourlyValues: List<String>,
    val dailyValues: List<String>,
    val pastDays: Int? = 1,
    val forecastDays: Int? = 3
)