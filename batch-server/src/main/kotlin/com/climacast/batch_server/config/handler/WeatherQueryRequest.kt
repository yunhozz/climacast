package com.climacast.batch_server.config.handler

import com.climacast.batch_server.common.enums.WeatherType

data class WeatherQueryRequest(
    val pastDays: Int?,
    val forecastDays: Int?,
    val weatherType: WeatherType
)