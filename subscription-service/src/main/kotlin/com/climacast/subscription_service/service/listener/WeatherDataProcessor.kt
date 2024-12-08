package com.climacast.subscription_service.service.listener

import com.climacast.global.dto.KafkaMessage
import com.climacast.subscription_service.dto.WeatherQueryDTO
import com.climacast.subscription_service.model.document.WeatherDocument
import reactor.core.publisher.Flux

interface WeatherDataProcessor {
    fun saveWeathersByMessageType(message: KafkaMessage): Flux<out WeatherDocument>
    fun lookupWeathers(query: WeatherQueryDTO): List<WeatherDocument>?
}