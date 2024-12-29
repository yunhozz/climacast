package com.climacast.subscription_service.model.repository

import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.model.entity.Subscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun findAllByIntervalsAndStatus(intervals: SubscriptionInterval, status: Boolean = true): List<Subscription>

    @Query("select distinct s.regions from Subscription s where s.id in :ids")
    fun findRegionsByIds(ids: List<Long>): Set<String>
}