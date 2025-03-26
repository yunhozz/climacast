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
    val data: Any
) : KafkaMessage

data class WeatherQueryResponseMessage(
    val data: Any
) : KafkaMessage