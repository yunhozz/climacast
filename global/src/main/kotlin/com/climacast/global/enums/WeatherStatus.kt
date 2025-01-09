package com.climacast.global.enums

enum class WeatherStatus(
    val codes: Array<Int>,
    val description: String,
    val icon: String
) {
    CLEAR(arrayOf(0), "Clear sky", "fas fa-sun"),
    MAINLY_CLEAR(arrayOf(1, 2, 3), "Mainly clear, partly cloudy, and overcast", "fas fa-cloud-sun"),
    FOG(arrayOf(45, 48), "Fog and depositing rime fog", "fas fa-smog"),
    DRIZZLE(arrayOf(51, 53, 55), "Drizzle: Light, moderate, and dense intensity", "fas fa-cloud-rain"),
    FREEZING_DRIZZLE(arrayOf(56, 57), "Freezing Drizzle: Light and dense intensity", "fas fa-snowflake"),
    RAIN(arrayOf(61, 63, 65), "Rain: Slight, moderate and heavy intensity", "fas fa-cloud-showers-heavy"),
    FREEZING_RAIN(arrayOf(66, 67), "Freezing Rain: Light and heavy intensity", "fas fa-icicles"),
    SNOW(arrayOf(71, 73, 75), "Snow fall: Slight, moderate, and heavy intensity", "fas fa-snowman"),
    SNOW_GRAINS(arrayOf(77), "Snow grains", "fas fa-snowflake"),
    RAIN_SHOWER(arrayOf(80, 81, 82), "Rain showers: Slight, moderate, and violent", "fas fa-cloud-showers-heavy"),
    SNOW_SHOWER(arrayOf(85, 86), "Snow showers slight and heavy", "fas fa-cloud-meatball"),
    THUNDERSTORM(arrayOf(95, 96, 99), "Thunderstorm", "fas fa-bolt")
    ;

    companion object {
        fun of(code: Int): WeatherStatus = entries.firstOrNull { it.codes.contains(code) }
            ?: throw IllegalArgumentException("Unknown weather status: $code")
    }
}