package com.climacast.batch_server.config

import com.climacast.global.enums.KafkaTopic
import com.climacast.global.event.KafkaMessage
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin.NewTopics
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import reactor.kafka.sender.SenderOptions

@Configuration
@EnableKafka
class KafkaProducerConfig(
    private val kafkaProperties: KafkaProperties
) {
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
    fun reactiveKafkaProducerTemplate(): ReactiveKafkaProducerTemplate<String, KafkaMessage> =
        ReactiveKafkaProducerTemplate(SenderOptions.create(kafkaProducerProperties()))

    @Bean
    fun kafkaProducerProperties(): Map<String, Any> = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to false // deactivate sticky partitioner
    )

    @Bean
    fun kafkaTopics(): NewTopics {
        val np = numPartitions.toInt()
        val rf = replicationFactor.toShort()
        return NewTopics(
            NewTopic(KafkaTopic.WEATHER_FORECAST_TOPIC, np, rf),
            NewTopic(KafkaTopic.WEATHER_HISTORY_TOPIC, np, rf)
        )
    }
}