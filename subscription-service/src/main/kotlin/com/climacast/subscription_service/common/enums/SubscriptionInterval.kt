package com.climacast.subscription_service.common.enums

enum class SubscriptionInterval(
    val interval: String
) {
    THIRTY_MINUTE("30분"),
    ONE_HOUR("1시간"),
    THREE_HOURS("3시간"),
    SIX_HOURS("6시간"),
    TWELVE_HOURS("12시간"),
    ONE_DAY("하루")
    ;

    companion object {
        fun of(interval: String): SubscriptionInterval = entries.find { it.interval == interval }
            ?: throw IllegalArgumentException("해당 구독 주기가 존재하지 않습니다: $interval")
    }
}