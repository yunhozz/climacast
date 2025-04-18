package com.climacast.ai_service.service

import com.climacast.ai_service.infra.kafka.KafkaTopicHandler
import com.climacast.ai_service.model.dto.WeatherQueryRequestDTO
import com.climacast.global.enums.KafkaTopic
import com.climacast.global.enums.WeatherType
import com.climacast.global.event.KafkaEvent
import com.climacast.global.event.message.WeatherQueryRequestMessage
import com.climacast.global.utils.logger
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class WeatherAiService(
    private val chatModel: OllamaChatModel,
    private val kafkaTopicHandler: KafkaTopicHandler
) {
    private val log = logger()

    fun processQuery(dto: WeatherQueryRequestDTO): Mono<String> {
        val requestId = UUID.randomUUID().toString()
        val request = WeatherQueryRequestMessage(
            requestId,
            weatherType = WeatherType.of(dto.weatherType),
            region = "${dto.parentRegion} ${dto.childRegion}",
            startTime = dto.startTime,
            endTime = dto.endTime
        )
        val event = KafkaEvent(KafkaTopic.WEATHER_QUERY_REQUEST_TOPIC, request)

        kafkaTopicHandler.publish(event)

        return kafkaTopicHandler.consume()
            .filter { it.originalRequestId == requestId }
            .takeUntil { it.isLast == true }
            .concatMap { message ->
                val systemMessage = SystemMessage(TEXT_CONTENT)
                val userMessage = UserMessage(message.toString())
                chatModel.stream(systemMessage, userMessage)
            }
            .collectList()
            .flatMap { responses ->
                val result = responses.joinToString("")
                Mono.just(result)
            }
    }

    fun processQueryStream(dto: WeatherQueryRequestDTO): Flux<String> {
        val requestId = UUID.randomUUID().toString()
        val request = WeatherQueryRequestMessage(
            requestId,
            weatherType = WeatherType.of(dto.weatherType),
            region = "${dto.parentRegion} ${dto.childRegion}",
            startTime = dto.startTime,
            endTime = dto.endTime
        )
        val event = KafkaEvent(KafkaTopic.WEATHER_QUERY_REQUEST_STREAM_TOPIC, request)

        kafkaTopicHandler.publish(event)

        return kafkaTopicHandler.consume()
            .flatMap { message ->
                val systemMessage = SystemMessage(TEXT_CONTENT)
                val userMessage = UserMessage(message.toString())
                chatModel.stream(systemMessage, userMessage)
            }
    }

    companion object {
        private const val TEXT_CONTENT = """
            You will be given structured weather data for a specific region in text format.
            
            DO NOT write or return any code. 
            Do NOT return Python, JavaScript, or any programming language.
            
            Your only task is to describe the weather trend in human-friendly natural language.
            Focus on overall trends like: 
            - temperature changes over time
            - wind speed changes
            - noticeable weather patterns (like rain, clear, or cloudy days)
            
            Make the summary concise and readable as if you're explaining to a non-technical person.
        """
    }
}