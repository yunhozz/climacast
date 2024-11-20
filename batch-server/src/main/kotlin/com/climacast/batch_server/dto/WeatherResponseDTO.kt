package com.climacast.batch_server.dto

data class WeatherResponseDTO(
    val latitude: Double,
    val longitude: Double,
    var parentRegion: String?,
    var childRegion: String?,
    val timezone: String,
    val elevation: Double,
    val hourly: HourlyDTO?,
    val daily: DailyDTO?
) {

    data class HourlyDTO(
        val time: List<String>,
        val weather_code: List<Int>?,
        val temperature_2m: List<Double>?,
        val temperature_80m: List<Double>?,
        val temperature_120m: List<Double>?,
        val temperature_180m: List<Double>?,
        val wind_speed_10m: List<Double>?,
        val wind_speed_80m: List<Double>?,
        val wind_speed_120m: List<Double>?,
        val wind_speed_180m: List<Double>?,
        val relative_humidity_2m: List<Int>?
    )

    data class DailyDTO(
        val time: List<String>,
        val weather_code: List<Int>?,
        val temperature_2m_max: List<Double>?,
        val temperature_2m_min: List<Double>?,
        val sunrise: List<String>?,
        val sunset: List<String>?,
        val precipitation_sum: List<Double>?
    )
}