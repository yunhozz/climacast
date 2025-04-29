package com.climacast.ai_service.service

import com.climacast.ai_service.common.enums.CacheNames
import com.climacast.ai_service.common.enums.CacheType
import com.climacast.ai_service.config.CacheBeanNames
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
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

@Service
@CacheConfig(
    cacheManager = CacheBeanNames.WEATHER_AI_QUERY_CACHE_MANAGER_BEAN,
    cacheNames = [CacheNames.WEATHER_AI_QUERY_CACHE_NAME]
)
class WeatherAiService(
    private val chatModel: OllamaChatModel,
    private val kafkaTopicHandler: KafkaTopicHandler
) {
    private val log = logger()

    @Cacheable
    fun processQuery(dto: WeatherQueryRequestDTO, sessionId: String): Mono<String> {
        val requestId = UUID.randomUUID().toString()
        publishWeatherQueryEvent(dto, KafkaTopic.WEATHER_QUERY_REQUEST_TOPIC, requestId)

        return kafkaTopicHandler.consume()
            .filter { it.originalRequestId == requestId }
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
            .cache(Duration.ofMinutes(CacheType.WEATHER_AI_QUERY_CACHE.expirationTime))
            .doOnSuccess {
                log.info("AI response success. Bytes=${it.toByteArray().size}")
            }
            .doOnError { ex ->
                log.error(ex.localizedMessage, ex)
            }
    }

    @Cacheable
    fun processQueryStream(dto: WeatherQueryRequestDTO): Flux<String> {
        val requestId = UUID.randomUUID().toString()
        publishWeatherQueryEvent(dto, KafkaTopic.WEATHER_QUERY_REQUEST_STREAM_TOPIC, requestId)

        return kafkaTopicHandler.consume()
            .filter { it.originalRequestId == requestId }
            .take(calculateDaysBetween(dto.startTime, dto.endTime))
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
            .cache(Duration.ofMinutes(CacheType.WEATHER_AI_QUERY_CACHE.expirationTime))
            .doOnComplete {
                log.info("AI response stream success.")
            }
            .doOnError { ex ->
                log.error(ex.localizedMessage, ex)
            }
    }

    private fun publishWeatherQueryEvent(dto: WeatherQueryRequestDTO, topic: String, requestId: String) {
        val (weatherType, parentRegion, childRegion, _, startTime, endTime) = dto
        val message = WeatherQueryRequestMessage(
            requestId,
            weatherType = WeatherType.of(weatherType),
            region = "$parentRegion $childRegion",
            startTime,
            endTime
        )
        val event = KafkaEvent(topic, message)

        kafkaTopicHandler.publish(event)
    }

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