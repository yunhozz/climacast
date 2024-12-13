package com.climacast.batch_server.config.handler

import com.climacast.batch_server.common.enums.WeatherParameters
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
import reactor.util.retry.Retry
import java.time.Duration
import kotlin.math.ceil

@Component
class KafkaMessageHandler(
    private val kafkaTemplate: KafkaTemplate<String, KafkaMessage>,
    private val reactiveKafkaProducerTemplate: ReactiveKafkaProducerTemplate<String, KafkaMessage>
) {
    private val log = logger()

    companion object {
        private val objectMapper = jacksonObjectMapper()
        private const val KAFKA_MAX_REQUEST_SIZE = (1024 * 1024).toDouble() // 1MB

        private fun calculateChunkSize(dataSize: Int, bytes: ByteArray): Int =
            dataSize / ceil(bytes.size / KAFKA_MAX_REQUEST_SIZE).toInt()
    }

    fun sendWeatherResponses(param: WeatherParameters, weatherData: List<WeatherResponseDTO>) {
        val bytes = objectMapper.writeValueAsString(weatherData).toByteArray()
        val chunkSize = calculateChunkSize(weatherData.size, bytes)

        Flux.fromIterable(weatherData.chunked(chunkSize))
            .flatMap { weathers ->
                val event = createKafkaEvent(param, weathers)
                reactiveKafkaProducerTemplate.send(event.topic, event.message)
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
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
                    message = KafkaMessage.ForecastWeathersMessage(weathers)
                )
            WeatherParameters.WEATHER_HISTORY ->
                KafkaEvent(
                    topic = KafkaTopic.WEATHER_HISTORY_TOPIC,
                    message = KafkaMessage.HistoryWeathersMessage(weathers)
                )
        }
}