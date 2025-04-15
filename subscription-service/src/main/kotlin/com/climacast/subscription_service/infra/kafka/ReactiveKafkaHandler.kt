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
            .filter { it.name != "initConsumer" }
            .map { it.name }
        log.info("Initiate Reactive Kafka Consumers! $consumers")
        queryAndPublishWeatherDataConsumer()
    }

    private fun queryAndPublishWeatherDataConsumer() {
        kafkaConsumer.receive()
            .flatMap { record ->
                val request = record.value() as WeatherQueryRequestMessage
                val query = WeatherQueryDTO(
                    request.weatherType,
                    request.region,
                    request.startTime,
                    request.endTime
                )

                weatherQueryProvider.find(query)
                    .flatMap { document ->
                        kafkaProducer.send(KafkaTopic.WEATHER_QUERY_RESPONSE_TOPIC,
                            WeatherQueryResponseMessage(document.toString())
                        )
                    }
                    .doOnError { ex -> log.error(ex.localizedMessage, ex) }
                    .doFinally { record.receiverOffset().acknowledge() }
            }
            .subscribe()
    }
}