package com.climacast.subscription_service.infra.kafka

import com.climacast.subscription_service.dto.WeatherQueryDTO
import com.climacast.subscription_service.model.document.WeatherDocument
import reactor.core.publisher.Mono

interface WeatherQueryProvider {
    fun find(query: WeatherQueryDTO): Mono<WeatherDocument>
}