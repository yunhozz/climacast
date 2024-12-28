package com.climacast.subscription_service.service.handler.subscription

import com.climacast.global.utils.logger
import jakarta.mail.MessagingException
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class MailHandler(
    private val mailSender: JavaMailSender,
    private val imageConverter: ImageConverter
) : SubscriptionHandler {

    private lateinit var email: String

    private val log = logger()

    override fun setSubscriberInfo(info: SubscriberInfo) {
        email = info.email!!
    }

    @Async
    override fun send(data: Any) {
        val weatherImage = imageConverter.convertHtmlToImage(data)
        val message = mailSender.createMimeMessage()
        try {
            MimeMessageHelper(message, true, "UTF-8").apply {
                setTo(email)
                setSubject("[Climacast] ${createCurrentTime()} Weather Information")
                addInline("image", weatherImage)
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