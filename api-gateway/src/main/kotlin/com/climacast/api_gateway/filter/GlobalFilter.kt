package com.climacast.api_gateway.filter

import com.climacast.global.utils.logger
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class GlobalFilter: AbstractGatewayFilterFactory<GlobalFilter.Config>(Config::class.java) {

    private val log = logger()

    override fun apply(config: Config?) = GatewayFilter { exchange, chain ->
        val request = exchange.request
        val response = exchange.response

        log.info("[Global Filter Start] Request ID -> ${request.id}")
        log.info("Request URI : ${request.uri}")

        chain.filter(exchange)
            .then(Mono.fromRunnable {
                log.info("[Global Filter End] Response Code -> ${response.statusCode}")
            })
    }

    data class Config(
        val baseMessage: String,
        val preLogger: Boolean,
        val postLogger: Boolean
    )
}