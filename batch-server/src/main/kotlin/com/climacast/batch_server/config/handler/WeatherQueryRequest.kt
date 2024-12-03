package com.climacast.batch_server.config.handler

import com.climacast.global.enums.WeatherType

data class WeatherQueryRequest(
    val pastDays: Int?,
    val forecastDays: Int?,
    val weatherType: WeatherType
)