package com.climacast.batch_server.common.util

import com.climacast.global.dto.WeatherResponseDTO
import java.util.concurrent.CopyOnWriteArraySet

object WeatherDataBuffer {
    private val weatherData = CopyOnWriteArraySet<WeatherResponseDTO>()

    fun store(data: List<WeatherResponseDTO>) {
        weatherData.addAll(data)
    }

    fun readAll(): List<WeatherResponseDTO> = weatherData.toList()

    fun clear() {
        weatherData.clear()
    }
}