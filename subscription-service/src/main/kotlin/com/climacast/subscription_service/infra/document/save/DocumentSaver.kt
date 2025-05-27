package com.climacast.subscription_service.infra.document.save

import com.climacast.global.event.KafkaMessage

interface DocumentSaver {
    fun saveWeathersByMessageType(message: KafkaMessage)
}