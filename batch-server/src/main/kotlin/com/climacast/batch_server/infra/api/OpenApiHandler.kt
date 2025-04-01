package com.climacast.batch_server.infra.api

import org.springframework.batch.item.Chunk
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class OpenApiHandler<I, O> {

    protected fun callWeatherOpenApi(chunk: Chunk<out I>, processor: (I) -> Mono<O>): MutableList<O>? =
        Flux.fromIterable(chunk.items)
            .flatMap { item -> processor(item) }
            .collectList()
            .block()

    abstract fun callOpenApi(chunk: Chunk<out I>, query: Any): List<O>
}