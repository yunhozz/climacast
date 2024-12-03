package com.climacast.global.dto

data class KafkaEvent(
    val topic: String,
    val message: KafkaMessage
)

sealed interface KafkaMessage {
    data class ForecastWeathersDTO(
        val data: List<WeatherResponseDTO>
    ): KafkaMessage

    data class HistoryWeathersDTO(
        val data: List<WeatherResponseDTO>
    ): KafkaMessage
}