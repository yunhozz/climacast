package com.climacast.subscription_service.service.handler.subscription

import com.climacast.global.utils.logger
import com.slack.api.Slack
import com.slack.api.webhook.Payload
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SlackHandler : SubscriptionHandler {

    @Value("\${slack.webhook.url}")
    private lateinit var webhookUrl: String

    private val log = logger()

    override fun setSubscriberInfo(info: SubscriberInfo) {}

    override fun send(data: Any) {
        val slack = Slack.getInstance()
        val payload = Payload.builder()
            .text(data.toString())
            .build()

        log.info("Send data to Slack: $payload")
        val response = slack.send(webhookUrl, payload)
        log.info(response.toString())
    }

    override fun getHandlerName() = SubscriptionHandlerName.SLACK
}