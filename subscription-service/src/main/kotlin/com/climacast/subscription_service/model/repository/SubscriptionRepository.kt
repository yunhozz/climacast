package com.climacast.subscription_service.model.repository

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.model.entity.Subscription
import com.climacast.subscription_service.model.entity.SubscriptionInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    @Query("""
        select distinct s.regions as regions, s.weatherType as weatherType, s.method as method, s.subscriptionInfo as subscriptionInfo
        from Subscription s
        where s.intervals = :interval and s.status = true
    """)
    fun findSubscriptionSummarySetByInterval(interval: SubscriptionInterval): Set<SubscriptionSummary>

    fun findAllByIntervalsAndStatus(intervals: SubscriptionInterval, status: Boolean = true): List<Subscription>
}

interface SubscriptionSummary {
    fun getRegions(): Set<String>
    fun getWeatherType(): WeatherType
    fun getMethod(): SubscriptionMethod
    fun getSubscriptionInfo(): SubscriptionInfo?
}