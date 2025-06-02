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
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.cache.annotation.CacheConfig
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

@Service
@CacheConfig(
    cacheManager = CacheBeanNames.WEATHER_AI_QUERY_CACHE_MANAGER_BEAN,
    cacheNames = [CacheNames.WEATHER_AI_QUERY_CACHE_NAME]
)
class WeatherAiService(
    private val chatModel: OpenAiChatModel,
    private val kafkaTopicHandler: KafkaTopicHandler,
    reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) {
    private val log = logger()
    private val ops = reactiveRedisTemplate.opsForValue()

    fun processAiSummary(dto: WeatherQueryRequestDTO, sessionId: String): Mono<String> {
        val cacheKey = "$sessionId::$dto"

        return ops.get(cacheKey)
            .flatMap { cachedAnswer -> Mono.just(cachedAnswer) }
            .switchIfEmpty {
                val requestId = UUID.randomUUID().toString()
                publishWeatherQueryEvent(dto, KafkaTopic.WEATHER_QUERY_REQUEST_TOPIC, requestId)

                kafkaTopicHandler.consumeWeatherQueryResponse()
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
                        createAnswerWithCache(cacheKey, chatModel.call(systemMessage, userMessage))
                    }
                    .cache(Duration.ofMinutes(CacheType.WEATHER_AI_QUERY_CACHE.expirationTime))
                    .doOnSuccess { answer ->
                        log.info("AI response success. Bytes=${answer.toByteArray().size}")
                    }
                    .doOnError { ex ->
                        log.error(ex.localizedMessage, ex)
                    }
            }
    }

    fun processAiSummaryStream(dto: WeatherQueryRequestDTO): Flux<String> {
        val requestId = UUID.randomUUID().toString()
        publishWeatherQueryEvent(dto, KafkaTopic.WEATHER_QUERY_REQUEST_STREAM_TOPIC, requestId)

        return kafkaTopicHandler.consumeWeatherQueryResponse()
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

    private fun createAnswerWithCache(key: String, answer: String): Mono<String> = ops
        .setIfAbsent(key, answer, Duration.ofMinutes(3))
        .thenReturn(answer)

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
            Given plain-text structured weather data for a region, summarize the weather trends in natural language.

            Focus on:
            - temperature changes
            - wind speed shifts
            - key weather patterns (e.g., rain, clouds, clear skies)

            Include the date. Keep it simple and clear for non-experts.
        """

        private const val SUMMARY_PROMPT = """
            Given multiple weather analyses, create a single summary.

            Combine key trends, highlight notable events, and avoid repetition.  
            Write clearly for a general audience.  
            Then, translate the summary into Korean.
        """
    }
}