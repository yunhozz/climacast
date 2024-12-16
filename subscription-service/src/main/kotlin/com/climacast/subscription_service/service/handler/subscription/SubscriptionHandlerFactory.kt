package com.climacast.subscription_service.service.handler.subscription

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SubscriptionHandlerFactory
    @Autowired constructor(handlers: Set<SubscriptionHandler>) {

    private val handlers = mutableMapOf<SubscriptionHandlerName, SubscriptionHandler>()

    init {
        handlers.forEach { handler ->
            this.handlers[handler.getHandlerName()] = handler
        }
    }

    fun findHandler(handlerName: SubscriptionHandlerName): SubscriptionHandler = handlers[handlerName]
        ?: throw IllegalArgumentException("Subscription handler with name $handlerName not found")
}