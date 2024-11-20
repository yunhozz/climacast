package com.climacast.batch_server.model.entity

import com.climacast.batch_server.common.enums.WeatherStatus
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
@DiscriminatorValue("DAILY")
class DailyWeather(
    parentRegion: String,
    childRegion: String,
    latitude: Double,
    longitude: Double,
    status: WeatherStatus,
    time: LocalDateTime,
    @Embedded
    val data: DailyWeatherData
): Weather(parentRegion, childRegion, latitude, longitude, status, time)

@Embeddable
data class DailyWeatherData(
    val weatherCode: Int?,
    val maxTemperature2m: Double?,
    val minTemperature2m: Double?,
    val sunrise: LocalDateTime?,
    val sunset: LocalDateTime?,
    val precipitationSum: Double?
)