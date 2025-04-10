package com.climacast.batch_server.model.dto

data class OpenApiQueryRequestDTO(
    val hourlyValues: List<String>? = null,
    val dailyValues: List<String>? = null,
    val pastDays: Int? = 1,
    val forecastDays: Int? = 7
)