package com.climacast.subscription_service.infra.kafka

import com.climacast.global.enums.KafkaTopic
import com.climacast.global.event.KafkaMessage
import com.climacast.global.event.message.WeatherQueryRequestMessage
import com.climacast.global.event.message.WeatherQueryResponseMessage
import com.climacast.global.utils.logger
import com.climacast.subscription_service.model.dto.WeatherQueryDTO
import jakarta.annotation.PostConstruct
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderResult
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberFunctions

@Component
class ReactiveKafkaHandler(
    private val kafkaProducer: ReactiveKafkaProducerTemplate<String, KafkaMessage>,
    private val kafkaConsumer: ReactiveKafkaConsumerTemplate<String, KafkaMessage>,
    private val weatherQueryProvider: WeatherQueryProvider
) {
    private val log = logger()

    @PostConstruct
    fun initConsumer() {
        val consumers = this::class.declaredMemberFunctions
            .filter { it.visibility == KVisibility.INTERNAL }
            .map { it.name }
        log.info("Initiate Reactive Kafka Consumers! $consumers")
        queryAndPublishWeatherDataConsumer()
    }

    internal fun queryAndPublishWeatherDataConsumer() {
        kafkaConsumer.receive()
            .flatMap { record ->
                val request = record.value() as WeatherQueryRequestMessage
                val query = WeatherQueryDTO(
                    request.weatherType,
                    request.region,
                    request.startTime,
                    request.endTime
                )

                when (record.topic()) {
                    KafkaTopic.WEATHER_QUERY_REQUEST_TOPIC -> {
                        weatherQueryProvider.findMono(query)
                            .flatMapMany { documentList ->
                                val lastIndex = documentList.lastIndex
                                Flux.fromIterable(documentList.withIndex())
                                    .flatMap { (index, document) ->
                                        val isLast = index == lastIndex
                                        sendWeatherQueryResponseTopic(request.requestId, document.toString(), isLast)
                                    }
                            }
                    }
                    KafkaTopic.WEATHER_QUERY_REQUEST_STREAM_TOPIC -> {
                        weatherQueryProvider.findFlux(query)
                            .flatMap { document ->
                                sendWeatherQueryResponseTopic(request.requestId, document.toString())
                            }
                    }
                    else -> Flux.empty()
                }
                .doOnError { ex -> log.error(ex.localizedMessage, ex) }
                .doFinally { record.receiverOffset().acknowledge() }
            }
            .subscribe()
    }

    private fun sendWeatherQueryResponseTopic(
        id: String,
        response: String,
        isLast: Boolean? = false
    ): Mono<SenderResult<Void>> = kafkaProducer.send(
        KafkaTopic.WEATHER_QUERY_RESPONSE_TOPIC,
        WeatherQueryResponseMessage(id, response, isLast)
    )
}