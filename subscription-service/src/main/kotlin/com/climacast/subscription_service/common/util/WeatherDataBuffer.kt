package com.climacast.subscription_service.common.util

import java.util.concurrent.ConcurrentHashMap

object WeatherDataBuffer {
    private val weatherData = ConcurrentHashMap<String, Any>()

    fun put(key: String, data: Any) {
        weatherData[key] = data
    }

    fun find(key: String) = weatherData[key]
        ?: throw IllegalArgumentException("Null for key: $key")

    fun clear() {
        weatherData.clear()
    }
}