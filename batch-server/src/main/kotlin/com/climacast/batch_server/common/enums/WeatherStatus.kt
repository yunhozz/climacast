package com.climacast.batch_server.common.enums

enum class WeatherStatus(
    val codes: Array<Int>,
    val description: String
) {
    CLEAR(arrayOf(0), "Clear sky"),
    MAINLY_CLEAR(arrayOf(1, 2, 3), "Mainly clear, partly cloudy, and overcast"),
    FOG(arrayOf(45, 48), "Fog and depositing rime fog"),
    DRIZZLE(arrayOf(51, 53, 55), "Drizzle: Light, moderate, and dense intensity"),
    FREEZING_DRIZZLE(arrayOf(56, 57), "Freezing Drizzle: Light and dense intensity"),
    RAIN(arrayOf(61, 63, 65), "Rain: Slight, moderate and heavy intensity"),
    FREEZING_RAIN(arrayOf(66, 67), "Freezing Rain: Light and heavy intensity"),
    SNOW(arrayOf(71, 73, 75), "Snow fall: Slight, moderate, and heavy intensity"),
    SNOW_GRAINS(arrayOf(77), "Snow grains"),
    RAIN_SHOWER(arrayOf(80, 81, 82), "Rain showers: Slight, moderate, and violent"),
    SNOW_SHOWER(arrayOf(85, 86), "Snow showers slight and heavy"),
    THUNDERSTORM(arrayOf(95, 96, 99), "Thunderstorm")
    ;

    companion object {
        fun of(code: Int): WeatherStatus = entries.firstOrNull { it.codes.contains(code) }
            ?: throw IllegalArgumentException("Unknown weather status: $code")
    }
}