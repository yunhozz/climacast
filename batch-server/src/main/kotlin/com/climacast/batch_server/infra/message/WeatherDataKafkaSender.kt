package com.climacast.batch_server.infra.message

import com.climacast.batch_server.common.enums.WeatherParameters
import com.climacast.global.dto.ForecastWeathersMessage
import com.climacast.global.dto.HistoryWeathersMessage
import com.climacast.global.dto.KafkaEvent
import com.climacast.global.dto.KafkaMessage
import com.climacast.global.dto.WeatherResponseDTO
import com.climacast.global.enums.KafkaTopic
import com.climacast.global.utils.logger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.math.ceil

@Component
class WeatherDataKafkaSender(
    private val kafkaTemplate: KafkaTemplate<String, KafkaMessage>
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
        log.info("""
            [Weather Data Information]
            bytes=${bytes.size}, chunkSize=$chunkSize
        """.trimIndent())

        weatherData.chunked(chunkSize).forEach { weathers ->
            val event = createKafkaEvent(param, weathers)
            kafkaTemplate.send(event.topic, event.message)
                .whenComplete { result, ex ->
                    val metadata = result.recordMetadata
                    log.info("""
                        [Send Weather Data on Kafka]
                        timestamp=${metadata.timestamp()}
                        topic=${metadata.topic()}
                        partition=${metadata.partition()}
                        offset=${metadata.offset()}
                    """.trimIndent())
                    if (ex != null) {
                        log.error("Kafka send failed: ${ex.localizedMessage}", ex)
                    }
                }
        }
    }

    private fun createKafkaEvent(param: WeatherParameters, weathers: List<WeatherResponseDTO>): KafkaEvent =
        when (param) {
            WeatherParameters.WEATHER_FORECAST ->
                KafkaEvent(
                    topic = KafkaTopic.WEATHER_FORECAST_TOPIC,
                    message = ForecastWeathersMessage(weathers)
                )
            WeatherParameters.WEATHER_HISTORY ->
                KafkaEvent(
                    topic = KafkaTopic.WEATHER_HISTORY_TOPIC,
                    message = HistoryWeathersMessage(weathers)
                )
        }
}