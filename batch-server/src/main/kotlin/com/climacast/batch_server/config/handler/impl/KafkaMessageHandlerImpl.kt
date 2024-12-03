package com.climacast.batch_server.config.handler.impl

import com.climacast.batch_server.common.enums.WeatherParameters
import com.climacast.batch_server.config.handler.KafkaMessageHandler
import com.climacast.global.dto.KafkaEvent
import com.climacast.global.dto.KafkaMessage
import com.climacast.global.dto.WeatherResponseDTO
import com.climacast.global.enums.KafkaTopic
import com.climacast.global.utils.logger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import kotlin.math.ceil

@Component
class KafkaMessageHandlerImpl(
    private val kafkaTemplate: KafkaTemplate<String, KafkaMessage>,
    private val reactiveKafkaProducerTemplate: ReactiveKafkaProducerTemplate<String, KafkaMessage>
): KafkaMessageHandler {

    companion object {
        private val log = logger()
        private val objectMapper = jacksonObjectMapper()
        const val KAFKA_MAX_REQUEST_SIZE = (1024 * 1024).toDouble() // 1MB
    }

    override fun sendWeatherResponses(param: WeatherParameters, weatherData: List<WeatherResponseDTO>) {
        val bytes = objectMapper.writeValueAsString(weatherData).toByteArray()
        val chunkSize = calculateChunkSize(weatherData.size, bytes)

        Flux.fromIterable(weatherData.chunked(chunkSize))
            .flatMap { weathers ->
                val event = createKafkaEvent(param, weathers)
                reactiveKafkaProducerTemplate.send(event.topic, event.message)
                    .doOnSuccess {
                        val metadata = it.recordMetadata()
                        log.info("Kafka send success : ${metadata.topic()} / ${metadata.offset()}")
                    }
                    .doOnError {
                        log.error("Kafka send error : ${it.localizedMessage}", it)
                    }
            }
            .subscribe()
    }

    private fun createKafkaEvent(param: WeatherParameters, weathers: List<WeatherResponseDTO>): KafkaEvent =
        when (param) {
            WeatherParameters.WEATHER_FORECAST ->
                KafkaEvent(
                    topic = KafkaTopic.WEATHER_FORECAST_TOPIC,
                    message = KafkaMessage.ForecastWeathersDTO(weathers)
                )
            WeatherParameters.WEATHER_HISTORY ->
                KafkaEvent(
                    topic = KafkaTopic.WEATHER_HISTORY_TOPIC,
                    message = KafkaMessage.HistoryWeathersDTO(weathers)
                )
        }

    private fun calculateChunkSize(dataSize: Int, bytes: ByteArray): Int =
        dataSize / ceil(bytes.size / KAFKA_MAX_REQUEST_SIZE).toInt()
}