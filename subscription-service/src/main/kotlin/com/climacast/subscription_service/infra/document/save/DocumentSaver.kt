package com.climacast.subscription_service.infra.document.save

import com.climacast.global.dto.KafkaMessage

interface DocumentSaver {
    fun saveWeathersByMessageType(message: KafkaMessage)
}