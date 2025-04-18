package com.climacast.global.event.message

import com.climacast.global.enums.WeatherType
import com.climacast.global.event.KafkaMessage

data class WeatherQueryRequestMessage(
    val requestId: String,
    val weatherType: WeatherType,
    val region: String,
    val startTime: String?,
    val endTime: String?
) : KafkaMessage