package com.climacast.subscription_service.service.handler.subscription

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class MailHandler(
    private val mailSender: JavaMailSender
) : SubscriptionHandler {

    private lateinit var email: String

    @Async
    override fun send() {
        val message = mailSender.createMimeMessage()

        MimeMessageHelper(message).apply {
            setTo(email)
            setSubject("[Climacast] ${createCurrentTime()} Weather Information")
            setText("This is weather information")
        }

        mailSender.send(message)
    }

    override fun setSubscriberInfo(info: SubscriberInfo) {
        email = info.email
    }

    override fun getHandlerName() = SubscriptionHandlerName.MAIL

    companion object {
        private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        fun createCurrentTime(): String = LocalDateTime.now().format(DATETIME_FORMATTER)
    }
}