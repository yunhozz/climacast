package com.climacast.batch_server.common.api

import com.climacast.global.enums.WeatherType

data class WeatherQueryRequest(
    val pastDays: Int?,
    val forecastDays: Int?,
    val weatherType: WeatherType
)