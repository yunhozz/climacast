package com.climacast.batch_server.dto

data class WeatherResponseDTO(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Double,
    val hourly: HourlyDTO,
    val daily: DailyDTO?
) {
    data class HourlyDTO(
        val time: List<String>,
        val temperature_2m: List<Double>
    )

    data class DailyDTO(
        val time: List<String>
    )
}