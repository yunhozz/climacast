package com.climacast.subscription_service.infra.listener

import com.climacast.global.dto.KafkaMessage
import com.climacast.global.enums.KafkaTopic
import com.climacast.global.utils.logger
import com.climacast.subscription_service.infra.document.save.DocumentSaver
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class WeatherDataListener(
    private val documentSaver: DocumentSaver
) {
    private val log = logger()

    @KafkaListener(topics = [KafkaTopic.WEATHER_FORECAST_TOPIC, KafkaTopic.WEATHER_HISTORY_TOPIC])
    fun receiveWeatherDataTopics(
        @Payload message: KafkaMessage,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        ack: Acknowledgment
    ) {
        log.info(
            """
            [Weather Data Received] 
            Topic: $topic
            Partition: $partition
            Offset: $offset
            Key: $key
            """.trimIndent()
        )
        documentSaver.saveWeathersByMessageType(message)
        ack.acknowledge()
    }

    @KafkaListener(topicPattern = ".*\\.DLT")
    fun receiveDeadLetterTopics(
        @Payload dlt: KafkaMessage,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long
    ) {
        log.error(
            """
            [Dead Letter Received] 
            Topic: $topic
            Partition: $partition
            Offset: $offset
            Key: $key
            Message: $dlt
            """.trimIndent()
        )
    }
}