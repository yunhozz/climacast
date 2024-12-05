package com.climacast.subscription_service.service

import com.climacast.global.dto.KafkaMessage
import com.climacast.global.utils.logger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class DeadLetterTopicListener {

    private val log = logger()

    @KafkaListener(topicPattern = ".*\\.DLT", groupId = "dead-letter")
    fun receive(
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