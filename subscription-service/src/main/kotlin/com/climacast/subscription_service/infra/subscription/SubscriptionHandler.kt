package com.climacast.subscription_service.infra.subscription

import com.climacast.subscription_service.common.enums.SubscriptionMethod

interface SubscriptionHandler {
    fun setSubscriberInfo(info: SubscriberInfo)
    fun send(data: Any)
    fun getHandlerMethod(): SubscriptionMethod
}