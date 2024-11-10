package com.climacast.batch_server.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.channel.ChannelOption
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
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
import javax.net.ssl.SSLException

@Configuration
class WebClientConfig {

    companion object {
        val mapper: ObjectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        const val HTTP_CONNECT_TIMEOUT_MILLIS = 30000
        const val HTTP_READ_TIMEOUT_SECONDS = 30
        const val HTTP_WRITE_TIMEOUT_SECONDS = 30
        const val HTTP_MAX_CONNECTIONS = 100
        const val HTTP_PENDING_ACQUIRE_TIMEOUT_SECONDS = 30L
        const val HTTP_MAX_IDLE_MINUTES = 1L
        const val SSL_HANDSHAKE_TIMEOUT_MILLIS = 30000L
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
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, HTTP_CONNECT_TIMEOUT_MILLIS)
            .secure {
                try {
                    it.sslContext(
                        SslContextBuilder.forClient()
                            // SSL 인증서 확인을 비활성화 => 즉, 클라이언트는 유효하지 않거나 신뢰할 수 없는 인증서라도 모든 SSL 인증서를 수락
                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                            .build()
                    ).handshakeTimeoutMillis(SSL_HANDSHAKE_TIMEOUT_MILLIS)
                } catch (e: SSLException) {
                    throw RuntimeException(e)
                }
            }
            .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(HTTP_READ_TIMEOUT_SECONDS))
                    .addHandlerLast(WriteTimeoutHandler(HTTP_WRITE_TIMEOUT_SECONDS))
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
            .maxConnections(HTTP_MAX_CONNECTIONS)
            .pendingAcquireTimeout(Duration.ofSeconds(HTTP_PENDING_ACQUIRE_TIMEOUT_SECONDS))
            .pendingAcquireMaxCount(-1)
            .maxIdleTime(Duration.ofMinutes(HTTP_MAX_IDLE_MINUTES))
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