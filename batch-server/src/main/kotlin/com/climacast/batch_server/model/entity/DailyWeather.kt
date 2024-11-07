package com.climacast.batch_server.model.entity

import com.climacast.batch_server.common.enums.WeatherStatus
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@DiscriminatorValue("DAILY")
class DailyWeather(
    parentRegion: String,
    childRegion: String,
    latitude: Double,
    longitude: Double,
    variable: WeatherStatus,
    val time: LocalDate,
    val data: DailyWeatherData
): Weather(parentRegion, childRegion, latitude, longitude, variable)

@Embeddable
data class DailyWeatherData(
    val code: Int,
    val maxTemperature2m: Double,
    val minTemperature2m: Double,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    val precipitationSum: Double
)