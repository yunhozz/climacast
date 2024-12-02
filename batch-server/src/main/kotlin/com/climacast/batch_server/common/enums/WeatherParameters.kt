package com.climacast.batch_server.common.enums

enum class WeatherParameters {
    WEATHER_FORECAST, WEATHER_HISTORY
    ;

    companion object {
        fun of(param: String?): WeatherParameters = entries.firstOrNull { it.name == param }
            ?: throw IllegalArgumentException("Unknown parameter '$param'")
    }
}