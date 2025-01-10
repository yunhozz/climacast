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

    fun store(datum: WeatherDatum, method: SubscriptionMethod) {
        val dataSet = weatherData[method]
        dataSet?.add(datum)
    }

    fun find(region: String, method: SubscriptionMethod): Any? {
        val dataSet = weatherData[method]
        return dataSet?.firstOrNull { it.region == region }?.resource
    }

    fun clear() {
        weatherData.clear()
    }
}

data class WeatherDatum(
    val region: String,
    val resource: Any
) : Comparable<WeatherDatum> {
    override fun compareTo(other: WeatherDatum): Int {
        val regionComparison = this.region.compareTo(other.region)
        val resourceComparison = this.resource == other.resource

        return when {
            regionComparison == 0 && resourceComparison -> 0
            else -> 1
        }
    }
}