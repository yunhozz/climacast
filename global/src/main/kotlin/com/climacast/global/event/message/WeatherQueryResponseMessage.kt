package com.climacast.global.event.message

import com.climacast.global.event.KafkaMessage

data class WeatherQueryResponseMessage(
    val originalRequestId: String,
    val weatherData: String,
    val isLast: Boolean?
) : KafkaMessage