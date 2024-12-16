package com.climacast.subscription_service.config

import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
@EnableConfigurationProperties(MailProperties::class)
class MailConfig(
    private val mailProperties: MailProperties
) {
    @Bean
    fun mailSender(): JavaMailSender =
        JavaMailSenderImpl().apply {
            host = mailProperties.host
            port = mailProperties.port
            username = mailProperties.username
            password = mailProperties.password
            defaultEncoding = "UTF-8"
        }
}