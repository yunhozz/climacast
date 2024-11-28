package com.climacast.batch_server.config.handler

import com.climacast.batch_server.config.Region
import com.climacast.batch_server.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.dto.WeatherResponseDTO
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class OpenApiHandler(
    protected val weatherResponseParser: WeatherResponseParser
) {
    private lateinit var regions: Set<Region>
    protected lateinit var dto: OpenApiQueryRequestDTO

    fun init(regions: Set<Region>, dto: OpenApiQueryRequestDTO) {
        this.regions = regions
        this.dto = dto
    }

    fun callWeatherOpenApi(
        requestHandler: (Region) -> Mono<WeatherResponseDTO>
    ): MutableList<WeatherResponseDTO>? =
        Flux.fromIterable(regions)
            .flatMap { region -> requestHandler(region) }
            .collectList()
            .block()

    abstract fun callForecastWeatherOpenApi(): List<WeatherResponseDTO>
    abstract fun callHistoricalWeatherOpenApi(): List<WeatherResponseDTO>
}