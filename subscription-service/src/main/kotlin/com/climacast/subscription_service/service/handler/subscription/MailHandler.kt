package com.climacast.subscription_service.service.handler.subscription

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class MailHandler(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) : SubscriptionHandler {

    private lateinit var email: String

    override fun setSubscriberInfo(info: SubscriberInfo) {
        email = info.email
    }

    @Async
    override fun send(data: Any) {
        val message = mailSender.createMimeMessage()
        MimeMessageHelper(message, true, "UTF-8").apply {
            setTo(email)
            setSubject("[Climacast] ${createCurrentTime()} Weather Information")
            setText(createHtmlTemplate(data), true)
        }
        mailSender.send(message)
    }

    override fun getHandlerName() = SubscriptionHandlerName.MAIL

    private fun createHtmlTemplate(data: Any): String {
        val context = Context()
        context.setVariable("weatherData", data)
        return templateEngine.process(WEATHER_MAIL_TEMPLATE, context)
    }

    companion object {
        const val WEATHER_MAIL_TEMPLATE = "weather_mail"
        private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        fun createCurrentTime(): String = LocalDateTime.now().format(DATETIME_FORMATTER)
    }
}