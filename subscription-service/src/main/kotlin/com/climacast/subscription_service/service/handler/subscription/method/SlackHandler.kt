package com.climacast.subscription_service.service.handler.subscription.method

import com.climacast.global.utils.logger
import com.climacast.subscription_service.service.handler.subscription.SubscriberInfo
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandler
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandlerName
import com.slack.api.Slack
import com.slack.api.methods.SlackApiException
import com.slack.api.model.Attachment
import com.slack.api.webhook.Payload
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException

@Component
class SlackHandler : SubscriptionHandler {

    @Value("\${slack.oauth.token}")
    private lateinit var authToken: String

    @Value("\${slack.channel.id}")
    private lateinit var channelId: String

    @Value("\${slack.webhook.url}")
    private lateinit var webhookUrl: String

    private val log = logger()

    override fun setSubscriberInfo(info: SubscriberInfo) {}

    override fun send(data: Any) {
        val slack = Slack.getInstance()
        val weatherImage = data as File

        try {
            val fileResponse = slack.methods(authToken)
                .filesUploadV2 {
                    it.file(weatherImage)
                        .channel(channelId)
                        .title("Weather Forecast Image")
                        .filename(weatherImage.nameWithoutExtension)
                }
            val payload = Payload.builder()
                .text("Here is Weather Forecast!")
                .attachments(listOf(
                    Attachment.builder()
                        .imageUrl(fileResponse.file.urlPrivate)
                        .fallback("Could not retrieve this image.")
                        .build()
                )).build()

            val response = slack.send(webhookUrl, payload)
            response.takeIf { it.code == 200 }?.let {
                log.info("Success to send data on Slack")
            } ?: run { log.error("Fail to send data on Slack: code=${response.code}, message=${response.body}") }

        } catch (e: Exception) {
            when (e) {
                is SlackApiException, is IOException -> throw IllegalArgumentException("Fail to send message on Slack", e)
                else -> throw IllegalArgumentException(e.localizedMessage, e)
            }
        }
    }

    override fun getHandlerName() = SubscriptionHandlerName.SLACK
}