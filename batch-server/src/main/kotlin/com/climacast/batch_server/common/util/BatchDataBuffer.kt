package com.climacast.batch_server.common.util

import com.climacast.global.dto.WeatherResponseDTO
import java.util.concurrent.CopyOnWriteArraySet

object BatchDataBuffer {
    private val weatherData = CopyOnWriteArraySet<WeatherResponseDTO>()
    private var iterator: Iterator<WeatherResponseDTO>? = null

    fun store(data: List<WeatherResponseDTO>) {
        weatherData.addAll(data)
    }

    fun read(): WeatherResponseDTO? {
        if (iterator == null) {
            iterator = weatherData.iterator()
        }
        return iterator?.takeIf { it.hasNext() }?.next()
    }

    fun clear() {
        weatherData.clear()
        iterator = null
    }
}