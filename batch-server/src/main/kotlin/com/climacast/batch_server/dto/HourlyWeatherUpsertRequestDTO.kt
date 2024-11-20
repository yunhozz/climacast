package com.climacast.batch_server.dto

import java.time.LocalDateTime

data class HourlyWeatherUpsertRequestDTO(
    val parentRegion: String,
    val childRegion: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val time: LocalDateTime,
    val weatherCode: Int?,
    val temperature2m: Double?,
    val temperature80m: Double?,
    val temperature120m: Double?,
    val temperature180m: Double?,
    val windSpeed10m: Double?,
    val windSpeed80m: Double?,
    val windSpeed120m: Double?,
    val windSpeed180m: Double?,
    val humidity2m: Int?
)