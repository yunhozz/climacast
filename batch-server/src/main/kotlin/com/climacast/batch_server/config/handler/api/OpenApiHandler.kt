package com.climacast.batch_server.config.handler.api

import org.springframework.batch.item.Chunk
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class OpenApiHandler<I, O> {
    private lateinit var items: Set<I>
    protected var query: Any? = null

    fun chunk(chunk: Chunk<out I>): OpenApiHandler<I, O> {
        this.items = chunk.toSet()
        return this
    }

    fun <T> query(query: T): OpenApiHandler<I, O> {
        this.query = query
        return this
    }

    fun callWeatherOpenApi(requestHandler: (I) -> Mono<O>): MutableList<O>? =
        Flux.fromIterable(items)
            .flatMap { item -> requestHandler(item) }
            .collectList()
            .block()

    abstract fun callOpenApi(): List<O>
}