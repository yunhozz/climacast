package com.climacast.subscription_service.service.handler.document

import com.climacast.global.dto.KafkaMessage
import com.climacast.subscription_service.model.document.WeatherDocument
import reactor.core.publisher.Flux

interface DocumentSaveHandler {
    fun saveWeathersByMessageType(message: KafkaMessage): Flux<out WeatherDocument>
}