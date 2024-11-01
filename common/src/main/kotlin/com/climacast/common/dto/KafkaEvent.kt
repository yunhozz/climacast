package com.climacast.common.dto

sealed interface KafkaMessage

data class KafkaEvent(
    val topic: String,
    val message: KafkaMessage
)