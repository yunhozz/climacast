package com.climacast.global.event.message

import com.climacast.global.event.KafkaMessage

data class WeatherQueryResponseMessage(
    val weatherData: String
) : KafkaMessage