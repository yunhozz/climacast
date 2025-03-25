package com.climacast.ai_service.service

import com.climacast.global.utils.logger
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.model.Media
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.util.MimeTypeUtils

@Service
class WeatherAiService(
    private val chatModel: OllamaChatModel
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
}