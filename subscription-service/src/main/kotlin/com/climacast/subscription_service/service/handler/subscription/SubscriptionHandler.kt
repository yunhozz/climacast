package com.climacast.subscription_service.service.handler.subscription

interface SubscriptionHandler {
    fun send()
    fun getHandlerName(): SubscriptionHandlerName
}