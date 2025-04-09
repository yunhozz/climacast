package com.climacast.subscription_service.infra.listener

import com.climacast.global.enums.KafkaTopic
import com.climacast.global.event.KafkaMessage
import com.climacast.global.utils.logger
import com.climacast.subscription_service.infra.document.save.DocumentSaver
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class WeatherDataListener(
    private val documentSaver: DocumentSaver
) {
    @KafkaListener(topics = [KafkaTopic.WEATHER_FORECAST_TOPIC, KafkaTopic.WEATHER_HISTORY_TOPIC])
    fun receiveWeatherDataTopics(
        @Headers headers: MessageHeaders,
        @Payload message: KafkaMessage,
        ack: Acknowledgment
    ) {
        logTopic(headers)
        documentSaver.saveWeathersByMessageType(message)
        ack.acknowledge()
    }

    @KafkaListener(topicPattern = ".*\\.DLT")
    fun receiveDeadLetterTopics(@Headers headers: MessageHeaders) {
        logDeadLetterTopic(headers)
    }

    companion object {
        private val logger = logger()

        fun logTopic(headers: MessageHeaders) {
            logger.info(buildString {
                append("\n[Topic Received]").appendLine()
                append("Topic: ").appendLine(headers[KafkaHeaders.RECEIVED_TOPIC])
                append("Partition: ").appendLine(headers[KafkaHeaders.RECEIVED_PARTITION])
                append("Offset: ").appendLine(headers[KafkaHeaders.ORIGINAL_OFFSET])
                append("Key: ").appendLine(headers[KafkaHeaders.RECEIVED_KEY])
            })
        }

        fun logDeadLetterTopic(headers: MessageHeaders) {
            logger.error(buildString {
                append("\n[DLT Received]").appendLine()
                append("Topic: ").appendLine(headers[KafkaHeaders.DLT_ORIGINAL_TOPIC])
                append("Partition: ").appendLine(headers[KafkaHeaders.DLT_ORIGINAL_PARTITION])
                append("Offset: ").appendLine(headers[KafkaHeaders.DLT_ORIGINAL_OFFSET])
                append("Timestamp: ").appendLine(headers[KafkaHeaders.DLT_ORIGINAL_TIMESTAMP])
                append("Exception Message: ").appendLine(headers[KafkaHeaders.DLT_EXCEPTION_MESSAGE])
                append("Raw Data: ").appendLine(headers[KafkaHeaders.RAW_DATA])
            })
        }
    }
}