package com.climacast.subscription_service.config

import com.climacast.global.enums.KafkaTopic
import com.climacast.global.event.KafkaMessage
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin.NewTopics
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.SenderOptions
import java.util.Collections

@Configuration
@EnableKafka
class KafkaConfig(
    private val kafkaProperties: KafkaProperties
) {
    companion object {
        const val GROUP_ID = "subscription-service-group"
        const val AUTO_OFFSET_RESET = "earliest"
        const val JSON_DESERIALIZER_TRUST_PACKAGE = "com.climacast.global.*"
    }

    @Value("\${app.kafka.num-partitions}")
    private lateinit var numPartitions: String

    @Value("\${app.kafka.replication-factor}")
    private lateinit var replicationFactor: String

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, KafkaMessage> = KafkaTemplate(kafkaProducerFactory())

    @Bean
    fun kafkaProducerFactory(): ProducerFactory<String, KafkaMessage> =
        DefaultKafkaProducerFactory(kafkaProducerProperties())

    @Bean
    fun kafkaConsumerFactory(): ConsumerFactory<String, KafkaMessage> =
        DefaultKafkaConsumerFactory(kafkaConsumerProperties())

    @Bean
    fun reactiveKafkaProducerTemplate(): ReactiveKafkaProducerTemplate<String, KafkaMessage> =
        ReactiveKafkaProducerTemplate(SenderOptions.create(kafkaProducerProperties()))

    @Bean
    fun reactiveKafkaConsumerTemplate(): ReactiveKafkaConsumerTemplate<String, KafkaMessage> =
        ReactiveKafkaConsumerTemplate(
            ReceiverOptions.create<String, KafkaMessage>(kafkaConsumerProperties())
                .subscription(Collections.synchronizedList(
                    listOf(
                        KafkaTopic.WEATHER_QUERY_REQUEST_TOPIC,
                        KafkaTopic.WEATHER_QUERY_REQUEST_STREAM_TOPIC
                    )
                ))
        )

    @Bean
    fun kafkaListenerContainerFactory() =
        ConcurrentKafkaListenerContainerFactory<String, KafkaMessage>().apply {
            consumerFactory = kafkaConsumerFactory()
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
            setConcurrency(3)
            setCommonErrorHandler(
                DefaultErrorHandler(
                    DeadLetterPublishingRecoverer(kafkaTemplate()),
                    FixedBackOff(1000, 3)
                )
            )
        }

    @Bean
    fun kafkaProducerProperties() = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
    )

    @Bean
    fun kafkaConsumerProperties() = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
        ConsumerConfig.GROUP_ID_CONFIG to GROUP_ID,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to AUTO_OFFSET_RESET,
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
        JsonDeserializer.TRUSTED_PACKAGES to JSON_DESERIALIZER_TRUST_PACKAGE
    )

    @Bean
    fun kafkaTopics(): NewTopics {
        val np = numPartitions.toInt()
        val rf = replicationFactor.toShort()
        return NewTopics(
            NewTopic(KafkaTopic.WEATHER_QUERY_RESPONSE_TOPIC, np, rf)
        )
    }
}