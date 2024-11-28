package com.climacast.batch_server.config.handler

import com.climacast.batch_server.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.dto.Region
import com.climacast.batch_server.dto.WeatherResponseDTO
import org.springframework.batch.item.Chunk
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class OpenApiHandler(
    protected val weatherResponseParser: WeatherResponseParser
) {
    private lateinit var regions: Set<Region>
    protected lateinit var dto: OpenApiQueryRequestDTO

    fun init(chunk: Chunk<out Region>, dto: OpenApiQueryRequestDTO) {
        this.regions = chunk.toSet()
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