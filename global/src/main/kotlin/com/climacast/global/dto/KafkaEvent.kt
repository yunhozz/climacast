package com.climacast.global.dto

data class KafkaEvent(
    val topic: String,
    val message: KafkaMessage
)

sealed interface KafkaMessage {
    data class ForecastWeathersMessage(
        val data: List<WeatherResponseDTO>
    ): KafkaMessage

    data class HistoryWeathersMessage(
        val data: List<WeatherResponseDTO>
    ): KafkaMessage
}