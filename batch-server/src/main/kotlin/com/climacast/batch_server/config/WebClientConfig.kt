package com.climacast.batch_server.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfig {

    companion object {
        val mapper: ObjectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Value("\${open-api.open-meteo.base-url}")
    private lateinit var openMeteoBaseUrl: String

    @Bean
    fun openMeteoWebClient(): WebClient =
        webClient().mutate()
            .baseUrl(openMeteoBaseUrl)
            .build()

    @Bean
    fun webClient(): WebClient {
        val httpClient = HttpClient.create(connectionProvider())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
            .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(30))
                    .addHandlerLast(WriteTimeoutHandler(30))
            }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .exchangeStrategies(exchangeStrategies())
            .build()
    }

    @Bean
    fun connectionProvider(): ConnectionProvider =
        ConnectionProvider.builder("")
            .maxConnections(100)
            .pendingAcquireTimeout(Duration.ofSeconds(30))
            .pendingAcquireMaxCount(-1)
            .maxIdleTime(Duration.ofMinutes(1))
            .build()

    @Bean
    fun exchangeStrategies(): ExchangeStrategies =
        ExchangeStrategies.builder()
            .codecs {
                it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON))
                it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON))
                it.defaultCodecs().maxInMemorySize(1024 * 1024)
            }
            .build()
}