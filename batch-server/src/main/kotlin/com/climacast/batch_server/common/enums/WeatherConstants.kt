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
}

object DailyConstants {
    const val WEATHER_CODE = "weather_code"
    const val TEMPERATURE_2M_MAX = "temperature_2m_max"
    const val TEMPERATURE_2M_MIN = "temperature_2m_min"
    const val WIND_SPEED_10M_MAX = "wind_speed_10m_max"
    const val WIND_GUESTS_10M_MIN = "wind_gusts_10m_max"
}