package com.climacast.subscription_service.infra.repository.query

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.model.document.WeatherDocument
import com.climacast.subscription_service.model.dto.WeatherQueryDTO
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface WeatherSearchQueryRepository {
    fun upsertWeatherDocuments(documents: List<WeatherDocument>, type: WeatherType)
    fun findWeatherByTypeAndRegion(query: WeatherQueryDTO): WeatherDocument?
    fun findWeatherListMonoByQuery(query: WeatherQueryDTO): Mono<List<WeatherDocument>>
    fun findWeatherFluxByQuery(query: WeatherQueryDTO): Flux<WeatherDocument>
}