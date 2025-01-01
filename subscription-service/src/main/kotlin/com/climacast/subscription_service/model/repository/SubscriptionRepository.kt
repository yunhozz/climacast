package com.climacast.subscription_service.model.repository

import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.model.entity.Subscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun findAllByIntervalsInAndStatus(intervals: Set<SubscriptionInterval>, status: Boolean = true): List<Subscription>

    @Query("select distinct s.regions as regions, s.method as method from Subscription s where s.id in :ids")
    fun findRegionsAndMethodSetByIds(ids: List<Long>): Set<RegionsAndMethod>
}

interface RegionsAndMethod {
    fun getRegions(): Set<String>
    fun getMethod(): SubscriptionMethod
}