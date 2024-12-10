package com.climacast.batch_server.common.enums

object HourlyConstants {
    const val WEATHER_CODE = "weather_code"
    const val TEMPERATURE_2M = "temperature_2m"
    const val TEMPERATURE_80M = "temperature_80m"
    const val TEMPERATURE_120M = "temperature_120m"
    const val TEMPERATURE_180M = "temperature_180m"
    const val WIND_SPEED_10M = "wind_speed_10m"
    const val WIND_SPEED_80M = "wind_speed_80m"
    const val WIND_SPEED_120M = "wind_speed_120m"
    const val WIND_SPEED_180M = "wind_speed_180m"
    const val HUMIDITY = "relative_humidity_2m"

    val ENTIRE: List<String> by lazy {
        this::class.members
            .filter { it.isFinal && it.returnType.classifier == String::class }
            .map { it.call() as String }
    }
}

object DailyConstants {
    const val WEATHER_CODE = "weather_code"
    const val TEMPERATURE_2M_MAX = "temperature_2m_max"
    const val TEMPERATURE_2M_MIN = "temperature_2m_min"
    const val TEMPERATURE_APPARENT_MAX = "apparent_temperature_max"
    const val TEMPERATURE_APPARENT_MIN = "apparent_temperature_min"
    const val SUNRISE = "sunrise"
    const val SUNSET = "sunset"
    const val DAYLIGHT_DURATION = "daylight_duration"
    const val SUNSHINE_DURATION = "sunshine_duration"
    const val UV_INDEX_MAX = "uv_index_max"
    const val UV_INDEX_CLEAR_SKY_MAX = "uv_index_clear_sky_max"
    const val PRECIPITATION_SUM = "precipitation_sum"
    const val RAIN_SUM = "rain_sum"
    const val SHOWERS_SUM = "showers_sum"
    const val SNOWFALL_SUM = "snowfall_sum"
    const val PRECIPITATION_HOURS = "precipitation_hours"
    const val PRECIPITATION_PROBABILITY_MAX = "precipitation_probability_max"
    const val WIND_SPEED_10M_MAX = "wind_speed_10m_max"
    const val WIND_GUSTS_10M_MAX = "wind_gusts_10m_max"
    const val WIND_DIRECTION_10M_DOMINANT = "wind_direction_10m_dominant"
    const val SHORTWAVE_RADIATION_SUM = "shortwave_radiation_sum"
    const val REFERENCE_EVAPOTRANSPIRATION = "et0_fao_evapotranspiration"

    val ENTIRE: List<String> by lazy {
        this::class.members
            .filter { it.isFinal && it.returnType.classifier == String::class }
            .map { it.call() as String }
    }
}