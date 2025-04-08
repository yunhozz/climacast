package com.climacast.subscription_service.infra.kafka

import com.climacast.subscription_service.dto.WeatherQueryDTO
import com.climacast.subscription_service.model.document.WeatherDocument
import com.climacast.subscription_service.service.WeatherDocumentService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class WeatherQueryProviderImpl(
    private val weatherDocumentService: WeatherDocumentService
) : WeatherQueryProvider {

    override fun find(query: WeatherQueryDTO): Mono<WeatherDocument> =
        weatherDocumentService.findWeatherByQuery(query)
}