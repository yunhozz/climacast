package com.climacast.global.event.message

import com.climacast.global.enums.WeatherType
import com.climacast.global.event.KafkaMessage

data class WeatherQueryRequestMessage(
    val weatherType: WeatherType,
    val parentRegion: String,
    val childRegion: String,
    val startTime: String,
    val endTime: String
) : KafkaMessage