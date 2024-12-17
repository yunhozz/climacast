package com.climacast.subscription_service.service.handler.subscription

import org.springframework.stereotype.Component

@Component
class SlackHandler : SubscriptionHandler {

    override fun send() {
        TODO("Not yet implemented")
    }

    override fun setSubscriberInfo(info: SubscriberInfo) {
        TODO("Not yet implemented")
    }

    override fun getHandlerName() = SubscriptionHandlerName.SLACK
}