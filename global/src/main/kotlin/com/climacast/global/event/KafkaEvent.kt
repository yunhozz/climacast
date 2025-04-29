package com.climacast.global.event

interface KafkaMessage

data class KafkaEvent(
    val topic: String,
    val message: KafkaMessage
)