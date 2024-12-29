package com.climacast.subscription_service.service.handler.subscription.method

import com.climacast.global.utils.logger
import com.climacast.subscription_service.service.handler.subscription.SubscriberInfo
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandler
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandlerName
import jakarta.mail.MessagingException
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
class MailHandler(
    private val mailSender: JavaMailSender
) : SubscriptionHandler {

    private lateinit var email: String

    private val log = logger()

    override fun setSubscriberInfo(info: SubscriberInfo) {
        email = info.email!!
    }

    @Async
    override fun send(data: Any) {
        val weatherImage = data as File
        val message = mailSender.createMimeMessage()

        try {
            MimeMessageHelper(message, true, "UTF-8").apply {
                setTo(email)
                setSubject("[Climacast] ${createCurrentTime()} Weather Information")
                addInline(UUID.randomUUID().toString(), weatherImage)
            }
            mailSender.send(message)
            log.info("Success to send data on Email: id=${message.messageID}, sent=${message.sentDate}")

        } catch (e: Exception) {
            when (e) {
                is MailException, is MessagingException ->
                    throw IllegalArgumentException("Fail to send data on Email: ${e.localizedMessage}", e)
                else -> throw IllegalArgumentException(e.localizedMessage, e)
            }
        }
    }

    override fun getHandlerName() = SubscriptionHandlerName.MAIL

    companion object {
        fun createCurrentTime(): String = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }
}