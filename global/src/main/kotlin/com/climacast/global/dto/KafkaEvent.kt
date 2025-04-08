package com.climacast.global.dto

import com.climacast.global.enums.WeatherType

sealed interface KafkaMessage

data class KafkaEvent(
    val topic: String,
    val message: KafkaMessage
)

data class ForecastWeathersMessage(
    val data: List<WeatherResponseDTO>
) : KafkaMessage

data class HistoryWeathersMessage(
    val data: List<WeatherResponseDTO>
) : KafkaMessage

data class WeatherQueryRequestMessage(
    val weatherType: WeatherType,
    val parentRegion: String,
    val childRegion: String,
    val startTime: String,
    val endTime: String
) : KafkaMessage

data class WeatherQueryResponseMessage(
    val weatherData: String
) : KafkaMessage