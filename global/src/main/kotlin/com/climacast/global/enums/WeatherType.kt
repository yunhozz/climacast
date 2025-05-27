package com.climacast.global.enums

enum class WeatherType(
    val type: String
) {
    FORECAST("날씨 예보"), HISTORY("날씨 히스토리");

    companion object {
        fun of(type: String): WeatherType = entries.find { it.type == type }
            ?: throw IllegalArgumentException("해당 날씨 타입이 존재하지 않습니다: $type")
    }
}