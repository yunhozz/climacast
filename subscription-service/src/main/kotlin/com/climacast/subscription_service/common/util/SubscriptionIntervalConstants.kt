package com.climacast.subscription_service.common.util

import com.climacast.subscription_service.common.enums.SubscriptionInterval

object SubscriptionIntervalConstants {
    val EVERY_THIRTY_MINUTE = setOf(SubscriptionInterval.THIRTY_MINUTE)
    val EVERY_ONE_HOUR = EVERY_THIRTY_MINUTE + SubscriptionInterval.ONE_HOUR
    val EVERY_THREE_HOUR = EVERY_ONE_HOUR + SubscriptionInterval.THREE_HOURS
    val EVERY_SIX_HOUR = EVERY_THREE_HOUR + SubscriptionInterval.SIX_HOURS
    val EVERY_TWELVE_HOUR = EVERY_SIX_HOUR + SubscriptionInterval.TWELVE_HOURS
    val EVERY_ONE_DAY = EVERY_TWELVE_HOUR + SubscriptionInterval.ONE_DAY
}