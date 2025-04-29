package com.climacast.subscription_service.infra.subscription.method

import com.climacast.global.utils.logger
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.exception.SubscriptionServiceException
import com.climacast.subscription_service.infra.subscription.SubscriberInfo
import com.climacast.subscription_service.infra.subscription.SubscriptionHandler
import com.slack.api.Slack
import com.slack.api.methods.SlackApiException
import com.slack.api.model.Attachment
import com.slack.api.webhook.Payload
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
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

    @Async
    override fun send(data: Any) {
        val slack = Slack.getInstance()
        val weatherImageBytes = data as ByteArray

        try {
            val fileResponse = slack.methods(authToken)
                .filesUploadV2 {
                    it.fileData(weatherImageBytes)
                        .channel(channelId)
                        .title("Weather Image")
                }

            val payload = Payload.builder()
                .text("Here is Weather Data!")
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
            log.error(e.localizedMessage, e)
            when (e) {
                is SlackApiException, is IOException ->
                    throw SubscriptionServiceException.WeatherDataSendFailException()
                else -> throw IllegalArgumentException(e.localizedMessage, e)
            }
        }
    }

    override fun getHandlerMethod() = SubscriptionMethod.SLACK
}