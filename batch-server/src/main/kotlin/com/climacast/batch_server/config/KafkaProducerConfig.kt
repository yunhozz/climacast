package com.climacast.batch_server.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
class KafkaProducerConfig {

    @Value("\${kafka.bootstrap-servers}")
    private lateinit var kafkaBootstrapServers: List<String>

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(kafkaProducerFactory())

    @Bean
    fun kafkaProducerFactory(): ProducerFactory<String, Any> =
        DefaultKafkaProducerFactory(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
            )
        )
}