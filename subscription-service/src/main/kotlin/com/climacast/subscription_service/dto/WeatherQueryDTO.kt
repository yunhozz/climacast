package com.climacast.subscription_service.dto

import com.climacast.global.enums.WeatherType

data class WeatherQueryDTO(
    val weatherType: WeatherType, // FORECAST, HISTORY
    val region: String?
)