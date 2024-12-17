package com.climacast.subscription_service.service.handler.subscription

interface SubscriptionHandler {
    fun send()
    fun setSubscriberInfo(info: SubscriberInfo)
    fun getHandlerName(): SubscriptionHandlerName
}

data class SubscriberInfo(
    val email: String
)