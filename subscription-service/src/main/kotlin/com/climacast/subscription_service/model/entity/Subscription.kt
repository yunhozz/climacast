package com.climacast.subscription_service.model.entity

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Subscription(
    @Embedded
    val subscriptionInfo: SubscriptionInfo,
    regions: Set<String>,
    @Enumerated(EnumType.STRING)
    val weatherType: WeatherType,
    intervals: SubscriptionInterval,
    method: SubscriptionMethod,
    status: Boolean = true
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ElementCollection(fetch = FetchType.EAGER, targetClass = String::class)
    var regions: Set<String> = regions
        protected set

    @Enumerated(EnumType.STRING)
    var intervals: SubscriptionInterval = intervals
        protected set

    @Enumerated(EnumType.STRING)
    var method: SubscriptionMethod = method
        protected set

    var status: Boolean = status
        protected set

    fun updateRegions(regions: Set<String>) {
        require(regions.isNotEmpty() && regions.size <= 5) { "Choose at least 1 and no more than 5 regions" }
        this.regions = regions
    }

    fun updateSubscriptionInterval(interval: SubscriptionInterval) {
        require(weatherType != WeatherType.HISTORY) { "Subscription interval of history weather must be one day" }
        this.intervals = interval
    }

    fun updateSubscriptionMethod(method: SubscriptionMethod) {
        this.method = method
    }

    fun cancelSubscription() {
        require(status) { "Already canceled" }
        status = false
    }
}

@Embeddable
data class SubscriptionInfo(
    val email: String?,
    val phoneNumber: String?
)