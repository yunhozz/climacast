package com.climacast.subscription_service.service.handler.document

import com.climacast.global.dto.KafkaMessage

interface DocumentSaveHandler {
    fun saveWeathersByMessageType(message: KafkaMessage)
}