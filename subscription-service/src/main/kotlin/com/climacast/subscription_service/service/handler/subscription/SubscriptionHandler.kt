package com.climacast.subscription_service.service.handler.subscription

interface SubscriptionHandler {
    fun setSubscriberInfo(info: SubscriberInfo)
    fun send(data: Any)
    fun getHandlerName(): SubscriptionHandlerName
}