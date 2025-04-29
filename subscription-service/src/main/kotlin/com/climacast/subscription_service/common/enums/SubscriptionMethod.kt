package com.climacast.subscription_service.common.enums

enum class SubscriptionMethod(
    val method: String
) {
    MAIL("이메일"), SLACK("슬랙"), SMS("SMS");

    companion object {
        fun of(method: String): SubscriptionMethod = entries.find { it.method == method }
            ?: throw IllegalArgumentException("해당 구독 방식이 존재하지 않습니다: $method")
    }
}