package com.climacast.subscription_service.model.dto

import com.climacast.subscription_service.model.entity.Subscription

data class SubscriptionResponseDTO private constructor(
    val email: String?,
    val phoneNumber: String?,
    val regions: Set<String>,
    val weatherType: String,
    val interval: String,
    val method: String,
    val status: Boolean
) {
    companion object {
        fun fromEntity(s: Subscription) = SubscriptionResponseDTO(
            s.subscriptionInfo.email,
            s.subscriptionInfo.phoneNumber,
            s.regions,
            s.weatherType.type,
            s.intervals.interval,
            s.method.method,
            s.status
        )
    }
}