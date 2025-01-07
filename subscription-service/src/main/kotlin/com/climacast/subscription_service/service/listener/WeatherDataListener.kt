package com.climacast.subscription_service.service.listener

import com.climacast.global.dto.KafkaMessage
import com.climacast.global.utils.logger
import com.climacast.subscription_service.service.handler.document.DocumentSaveHandler
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.Date

@Component
class WeatherDataListener(
    private val reactiveKafkaConsumer: ReactiveKafkaConsumerTemplate<String, KafkaMessage>,
    private val documentSaveHandler: DocumentSaveHandler
) {
    private val log = logger()

    @EventListener
    fun saveWeathers(event: ApplicationReadyEvent) {
        log.info("ApplicationReadyEvent triggered for application: " +
                "${event.applicationContext.applicationName}, " +
                "time: ${Date(event.timestamp)}")

        reactiveKafkaConsumer.receive()
            .flatMap { record ->
                log.info("Received from topic=${record.topic()}, offset=${record.offset()}")
                try {
                    documentSaveHandler.saveWeathersByMessageType(record.value())
                    Mono.just(record)

                } catch (e: Exception) {
                    log.error("Failed to save weather data: ${e.message}")
                    Mono.empty()
                }
            }
            .flatMap { record ->
                record.receiverOffset().commit()
            }
            .subscribe()
    }
}