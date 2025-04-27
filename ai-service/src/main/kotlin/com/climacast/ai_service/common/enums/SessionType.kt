package com.climacast.ai_service.common.enums

enum class SessionType(
    val sessionName: String,
    val ttl: Long
) {
    DEFAULT_SESSION("default-session", 30L),
    WEATHER_AI_QUERY_SESSION("weather-ai-query-session", 3L)
}