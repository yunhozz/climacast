package com.climacast.ai_service.infra.kafka

import com.climacast.ai_service.common.exception.AiServiceException
import com.climacast.global.event.KafkaEvent
import com.climacast.global.event.KafkaMessage
import com.climacast.global.event.message.WeatherQueryResponseMessage
import com.climacast.global.utils.logger
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.Duration

@Component
class KafkaTopicHandler(
    private val kafkaProducer: ReactiveKafkaProducerTemplate<String, KafkaMessage>,
    private val kafkaConsumer: ReactiveKafkaConsumerTemplate<String, KafkaMessage>
) {
    private val log = logger()

    fun publish(event: KafkaEvent) {
        kafkaProducer.send(event.topic, event.message)
            .doOnSuccess { result ->
                val metadata = result.recordMetadata()
                log.info("""
                    [Kafka Topic Information]
                    timestamp=${metadata.timestamp()}
                    topic=${metadata.topic()}
                    partition=${metadata.partition()}
                    offset=${metadata.offset()}
                """.trimIndent())
            }
            .doOnError { ex ->
                log.error("Kafka send failure: ${ex.localizedMessage}", ex)
            }
            .subscribe({},
                { throw AiServiceException.KafkaSendException() }
            )
    }

    fun consumeWeatherQueryResponse(): Flux<WeatherQueryResponseMessage> = kafkaConsumer.receive()
        .doOnNext { record ->
            log.info("""
                [Consumed Message]
                ${record.value()}
            """.trimIndent())
            record.receiverOffset().acknowledge()
        }
        .doOnError { ex -> log.error(ex.localizedMessage, ex) }
        .map { it.value() as WeatherQueryResponseMessage }
        .timeout(Duration.ofMinutes(3))
        .onErrorMap { ex ->
            log.error(ex.localizedMessage, ex)
            throw AiServiceException.WeatherDataResponseTimeoutException()
        }
}