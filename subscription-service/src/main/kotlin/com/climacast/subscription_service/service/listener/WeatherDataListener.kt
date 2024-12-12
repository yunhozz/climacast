package com.climacast.subscription_service.service.listener

import com.climacast.global.dto.KafkaMessage
import com.climacast.global.utils.logger
import com.climacast.subscription_service.service.handler.WeatherDataHandler
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component

@Component
class WeatherDataListener(
    private val reactiveKafkaConsumer: ReactiveKafkaConsumerTemplate<String, KafkaMessage>,
    private val weatherDataHandler: WeatherDataHandler
) {
    private val log = logger()

    @EventListener(ApplicationReadyEvent::class)
    fun saveWeathers() {
        reactiveKafkaConsumer.receiveAutoAck()
            .doOnNext {
                log.info("Received from topic={}, offset={}", it.topic(), it.offset())
            }
            .map { it.value() }
            .flatMap { message ->
                weatherDataHandler.saveWeathersByMessageType(message)
            }
            .subscribe()
    }
}