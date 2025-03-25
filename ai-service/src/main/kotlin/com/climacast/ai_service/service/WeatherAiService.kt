package com.climacast.ai_service.service

import com.climacast.ai_service.dto.WeatherQueryRequestDTO
import com.climacast.ai_service.infra.kafka.KafkaTopicHandler
import com.climacast.global.dto.KafkaEvent
import com.climacast.global.dto.WeatherQueryRequestMessage
import com.climacast.global.enums.KafkaTopic
import com.climacast.global.utils.logger
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.content.Media
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.util.MimeTypeUtils
import reactor.core.publisher.Flux

@Service
class WeatherAiService(
    private val chatModel: OllamaChatModel,
    private val kafkaTopicHandler: KafkaTopicHandler
) {
    private val log = logger()

    fun test1(message: String): String = chatModel.call(message)

    fun test2(): String {
        val resource = ClassPathResource("image/test-image.png")
        val message = UserMessage(
            "Explain what do you see on this picture?",
            Media(MimeTypeUtils.IMAGE_PNG, resource)
        )

        /*
        Multimodal 만 이미지 인식 가능 (ex. LLAVA)
         */
        val chatResponse = chatModel.call(Prompt(message))
        val generation = chatResponse.result

        log.info(generation.toString())

        return generation.output.text
    }

    fun processQuery(query: WeatherQueryRequestDTO): Flux<String> {
        val (parentRegion, childRegion, _, startTime, endTime) = query
        val event = KafkaEvent(
            KafkaTopic.WEATHER_QUERY_REQUEST_TOPIC,
            WeatherQueryRequestMessage(parentRegion, childRegion, startTime.toString(), endTime.toString())
        )
        kafkaTopicHandler.publish(event)

        val systemMessage = SystemMessage("""
            사용자의 질문에서 Parent Region, Child Region, 시간대에 대해 Array 형식으로 나타내줘.
            이외의 답변은 필요 없고 Array 형식의 String 만 답변하면 돼.
        """.trimIndent())
        val userMessage = UserMessage(query.message)
        return chatModel.stream(systemMessage, userMessage)
    }
}