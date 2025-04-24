package com.climacast.ai_service.service

import com.climacast.ai_service.infra.kafka.KafkaTopicHandler
import com.climacast.ai_service.model.dto.WeatherQueryRequestDTO
import com.climacast.global.enums.DateTimePattern
import com.climacast.global.enums.KafkaTopic
import com.climacast.global.enums.WeatherType
import com.climacast.global.event.KafkaEvent
import com.climacast.global.event.message.WeatherQueryRequestMessage
import com.climacast.global.utils.DateTimeConverter
import com.climacast.global.utils.logger
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

@Service
class WeatherAiService(
    private val chatModel: OllamaChatModel,
    private val kafkaTopicHandler: KafkaTopicHandler
) {
    private val log = logger()

    fun processQuery(dto: WeatherQueryRequestDTO): Mono<String> {
        val (weatherType, parentRegion, childRegion, _, startTime, endTime) = dto
        val message = createQueryMessage(
            weatherType = WeatherType.of(weatherType),
            region = "$parentRegion $childRegion",
            startTime,
            endTime
        )
        val event = KafkaEvent(KafkaTopic.WEATHER_QUERY_REQUEST_TOPIC, message)
        kafkaTopicHandler.publish(event)

        return kafkaTopicHandler.consume()
            .filter { it.originalRequestId == message.requestId }
            .takeUntil { it.isLast == true }
            .flatMapSequential { response ->
                val systemMessage = SystemMessage(ANALYZE_PROMPT)
                val userMessage = UserMessage(response.weatherData)
                chatModel.stream(systemMessage, userMessage)
            }
            .collectList()
            .flatMap { responses ->
                val result = responses.joinToString("")
                val systemMessage = SystemMessage(SUMMARY_PROMPT)
                val userMessage = UserMessage(result)
                Mono.just(chatModel.call(systemMessage, userMessage))
            }
            .doOnSuccess {
                log.info("AI response success. Bytes=${it.toByteArray().size}")
            }
            .doOnError { ex ->
                log.error(ex.localizedMessage, ex)
            }
    }

    fun processQueryStream(dto: WeatherQueryRequestDTO): Flux<String> {
        val (weatherType, parentRegion, childRegion, _, startTime, endTime) = dto
        val message = createQueryMessage(
            weatherType = WeatherType.of(weatherType),
            region = "$parentRegion $childRegion",
            startTime,
            endTime
        )
        val event = KafkaEvent(KafkaTopic.WEATHER_QUERY_REQUEST_STREAM_TOPIC, message)
        kafkaTopicHandler.publish(event)

        return kafkaTopicHandler.consume()
            .filter { it.originalRequestId == message.requestId }
            .take(calculateDaysBetween(startTime, endTime))
            .publish { messageFlux ->
                val responses = CopyOnWriteArrayList<String>()
                messageFlux.concatMap { message ->
                    val systemMessage = SystemMessage(ANALYZE_PROMPT)
                    val userMessage = UserMessage(message.weatherData)
                    chatModel.stream(systemMessage, userMessage)
                        .collectList()
                        .map { it.joinToString("") }
                        .doOnNext { responses.add(it) }
                }.thenMany(
                    Flux.defer {
                        val result = responses.joinToString("")
                        val systemMessage = SystemMessage(SUMMARY_PROMPT)
                        val userMessage = UserMessage(result)
                        chatModel.stream(systemMessage, userMessage)
                    }
                )
            }
            .doOnComplete {
                log.info("AI response stream success.")
            }
            .doOnError { ex ->
                log.error(ex.localizedMessage, ex)
            }
    }

    private fun createQueryMessage(
        weatherType: WeatherType,
        region: String,
        startTime: String?,
        endTime: String?
    ) = WeatherQueryRequestMessage(
        requestId = UUID.randomUUID().toString(),
        weatherType = weatherType,
        region = region,
        startTime = startTime,
        endTime = endTime
    )

    private fun calculateDaysBetween(startTime: String?, endTime: String?) =
        if (startTime == null && endTime == null) 1
        else {
            val dateTimePattern = DateTimePattern.ELASTICSEARCH_PATTERN
            val st = DateTimeConverter.convertToLocalDateTime(startTime, dateTimePattern)
            val et = DateTimeConverter.convertToLocalDateTime(endTime, dateTimePattern)
            Duration.between(st, et).toDays() + 1
        }

    companion object {
        private const val ANALYZE_PROMPT = """
            You will receive structured weather data for a specific region in plain text format.
            Your task is to analyze and describe the weather trends in natural, human-readable language.
            
            Focus on patterns such as:
            - temperature trends over time
            - changes in wind speed
            - significant weather patterns (e.g., rain, clear skies, cloudy periods)
            
            Specify the date in the analysis result.
            Keep the summary clear and concise, as if explaining it to someone without a technical background.
        """

        private const val SUMMARY_PROMPT = """
            You will now be given several weather analysis results previously generated.

            Your task is to read through all of them and create a single cohesive summary.  
            Identify common patterns, highlight notable weather events, and present the overall trend.
            
            Write your summary in clear, natural language that anyone can understand.  
            Avoid repeating information — instead, synthesize the key insights into a compact report.
        """
    }
}