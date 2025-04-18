package com.climacast.subscription_service.infra.kafka

import com.climacast.subscription_service.model.document.WeatherDocument
import com.climacast.subscription_service.model.dto.WeatherQueryDTO
import com.climacast.subscription_service.service.WeatherDocumentService
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class WeatherQueryProviderImpl(
    private val weatherDocumentService: WeatherDocumentService
) : WeatherQueryProvider {

    override fun findMono(query: WeatherQueryDTO): Mono<List<WeatherDocument>> =
        weatherDocumentService.findWeatherListMonoByQuery(query)

    override fun findFlux(query: WeatherQueryDTO): Flux<WeatherDocument> =
        weatherDocumentService.findWeatherFluxByQuery(query)
}