package com.climacast.subscription_service.service.handler.subscription

import com.climacast.subscription_service.common.enums.SubscriptionMethod
import org.springframework.stereotype.Component

@Component
class SubscriptionHandlerFactory(handlers: Set<SubscriptionHandler>) {

    private val handlerMap: Map<SubscriptionMethod, SubscriptionHandler> =
        handlers.associateBy { it.getHandlerMethod() }

    fun createHandlerByMethod(method: SubscriptionMethod): SubscriptionHandler =
        handlerMap[method]
            ?: throw IllegalArgumentException("No SubscriptionHandler found for SubscriptionMethod: $method")
}