package com.climacast.global.dto

import com.climacast.global.enums.WeatherType

data class WeatherResponseDTO(
    val latitude: Double,
    val longitude: Double,
    var parentRegion: String?,
    var childRegion: String?,
    val timezone: String,
    val elevation: Double,
    val hourly: HourlyDTO?,
    val daily: DailyDTO?,
    var weatherType: WeatherType?
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
        val apparent_temperature_max: List<Double>?,
        val apparent_temperature_min: List<Double>?,
        val sunrise: List<String>?,
        val sunset: List<String>?,
        val daylight_duration: List<Double>?,
        val sunshine_duration: List<Double>?,
        val uv_index_max: List<Double>?,
        val uv_index_clear_sky_max: List<Double>?,
        val precipitation_sum: List<Double>?,
        val rain_sum: List<Double>?,
        val showers_sum: List<Double>?,
        val snowfall_sum: List<Double>?,
        val precipitation_hours: List<Double>?,
        val precipitation_probability_max: List<Double>?,
        val wind_speed_10m_max: List<Double>?,
        val wind_gusts_10m_max: List<Double>?,
        val wind_direction_10m_dominant: List<Int>?,
        val shortwave_radiation_sum: List<Double>?,
        val et0_fao_evapotranspiration: List<Double>?
    )
}