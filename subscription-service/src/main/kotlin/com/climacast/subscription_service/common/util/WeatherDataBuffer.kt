package com.climacast.subscription_service.common.util

import java.util.concurrent.ConcurrentHashMap

object WeatherDataBuffer {
    private val weatherData = ConcurrentHashMap<String, Any>()

    fun put(key: String, data: Any) {
        weatherData[key] = data
    }

    fun find(key: String) = weatherData.remove(key)
}