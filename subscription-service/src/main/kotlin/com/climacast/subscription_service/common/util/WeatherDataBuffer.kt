package com.climacast.subscription_service.common.util

import com.climacast.subscription_service.common.enums.SubscriptionMethod
import java.util.concurrent.ConcurrentSkipListSet

object WeatherDataBuffer {

    private val weatherData = mutableMapOf<SubscriptionMethod, ConcurrentSkipListSet<WeatherDatum>>()

    init {
        SubscriptionMethod.entries.forEach { method ->
            weatherData[method] = ConcurrentSkipListSet()
        }
    }

    fun store(region: String, resource: Any, method: SubscriptionMethod) {
        val dataSet = weatherData[method]
        dataSet?.add(WeatherDatum(region, resource))
    }

    fun find(region: String, method: SubscriptionMethod): Any? {
        val dataSet = weatherData[method]
        return dataSet?.firstOrNull { it.region == region }?.resource
    }

    fun clear() {
        weatherData.clear()
    }

    data class WeatherDatum(val region: String, val resource: Any): Comparable<WeatherDatum> {
        override fun compareTo(other: WeatherDatum): Int {
            val regionComparison = this.region.compareTo(other.region)
            if (regionComparison != 0) {
                return regionComparison
            }

            return when {
                this.resource == other.resource -> 0
                else -> 1
            }
        }
    }
}