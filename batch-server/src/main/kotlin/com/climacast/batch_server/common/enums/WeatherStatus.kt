package com.climacast.batch_server.common.enums

enum class WeatherStatus(
    val codes: List<Int>,
    val description: String
) {
    CLEAR(listOf(0), "Clear sky"),
    MAINLY_CLEAR(listOf(1, 2, 3), "Mainly clear, partly cloudy, and overcast"),
    FOG(listOf(45, 48), "Fog and depositing rime fog"),
    DRIZZLE(listOf(51, 53, 55), "Drizzle: Light, moderate, and dense intensity"),
    FREEZING_DRIZZLE(listOf(56, 57), "Freezing Drizzle: Light and dense intensity"),
    RAIN(listOf(61, 63, 65), "Rain: Slight, moderate and heavy intensity"),
    FREEZING_RAIN(listOf(66, 67), "Freezing Rain: Light and heavy intensity"),
    SNOW(listOf(71, 73, 75), "Snow fall: Slight, moderate, and heavy intensity"),
    SNOW_GRAINS(listOf(77), "Snow grains"),
    RAIN_SHOWER(listOf(80, 81, 82), "Rain showers: Slight, moderate, and violent"),
    SNOW_SHOWER(listOf(85, 86), "Snow showers slight and heavy"),
    THUNDERSTORM(listOf(95, 96, 99), "Thunderstorm")
    ;

    companion object {
        fun of(code: Int): WeatherStatus = entries.firstOrNull { it.codes.contains(code) }
            ?: throw IllegalArgumentException("Unknown weather status: $code")
    }
}