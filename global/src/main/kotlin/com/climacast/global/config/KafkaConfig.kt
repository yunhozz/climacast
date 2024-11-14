package com.climacast.global.config

import com.climacast.global.dto.KafkaMessage
import com.climacast.global.utils.logger
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff

@Configuration
@EnableKafka
class KafkaConfig {

    private val log = logger()

    @Value("\${kafka.bootstrap-servers}")
    private lateinit var kafkaBootstrapServers: List<String>

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, KafkaMessage> =
        KafkaTemplate(kafkaProducerFactory())

    @Bean
    fun kafkaProducerFactory(): ProducerFactory<String, KafkaMessage> =
        DefaultKafkaProducerFactory(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
            )
        )

    @Bean
    fun kafkaConsumerFactory(): ConsumerFactory<String, KafkaMessage> =
        DefaultKafkaConsumerFactory(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false // Kafka Consumer 가 메시지를 수동으로 커밋(acknowledge)
            )
        )

    @Bean
    fun kafkaListenerContainerFactory(
        kafkaTemplate: KafkaTemplate<String, KafkaMessage>,
        kafkaConsumerFactory: ConsumerFactory<String, KafkaMessage>
    ) = ConcurrentKafkaListenerContainerFactory<String, KafkaMessage>().apply {
        consumerFactory = kafkaConsumerFactory
        containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        setRecordMessageConverter(StringJsonMessageConverter())
        setCommonErrorHandler(
            DefaultErrorHandler(
                DeadLetterPublishingRecoverer(kafkaTemplate) { record, exception ->
                    log.error(
                        "[Kafka Consumer Error] topic='{}', key='{}', value='{}', error message='{}'",
                        record.topic(),
                        record.key(),
                        record.value(),
                        exception.message
                    )
                    TopicPartition(record.topic() + ".dlc", record.partition())
                }, FixedBackOff(1000, 3)
            )
        )
    }
}