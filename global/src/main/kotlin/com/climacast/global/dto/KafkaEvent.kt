package com.climacast.global.dto

data class KafkaEvent(
    val topic: String,
    val message: Any
)