package com.climacast.subscription_service.infra.kafka

import com.climacast.subscription_service.model.document.WeatherDocument
import com.climacast.subscription_service.model.dto.WeatherQueryDTO
import reactor.core.publisher.Mono

interface WeatherQueryProvider {
    fun find(query: WeatherQueryDTO): Mono<WeatherDocument>
}