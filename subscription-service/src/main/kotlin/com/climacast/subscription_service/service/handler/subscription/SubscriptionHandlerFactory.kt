package com.climacast.subscription_service.service.handler.subscription

import com.climacast.subscription_service.common.enums.SubscriptionMethod
import org.springframework.stereotype.Component

@Component
class SubscriptionHandlerFactory(handlers: Set<SubscriptionHandler>) {

    private val handlerMap = mutableMapOf<SubscriptionMethod, SubscriptionHandler>()

    init {
        handlers.forEach { handlerMap[it.getHandlerMethod()] = it }
    }

    fun createHandlerByMethod(method: SubscriptionMethod): SubscriptionHandler = handlerMap[method]
        ?: throw IllegalArgumentException("Subscription handler with $method not found")
}