package com.climacast.ai_service.common.enums

enum class CacheType(
    val cacheName: String,
    val expirationTime: Long
) {
    DEFAULT_CACHE(CacheNames.DEFAULT_CACHE_NAME, 1L),
    WEATHER_AI_QUERY_CACHE(CacheNames.WEATHER_AI_QUERY_CACHE_NAME, 3L)
}

object CacheNames {
    const val DEFAULT_CACHE_NAME = "default-cache"
    const val WEATHER_AI_QUERY_CACHE_NAME = "weather-ai-query-cache"
}