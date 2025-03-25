package com.climacast.global.dto

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
    val parentRegion: String,
    val childRegion: String,
    val startTime: String,
    val endTime: String
) : KafkaMessage

data class WeatherQueryResponseMessage(
    val weatherData: String
) : KafkaMessage