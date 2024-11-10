package com.climacast.batch_server.model.entity

import com.climacast.batch_server.common.enums.WeatherStatus
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
@DiscriminatorValue("HOURLY")
class HourlyWeather(
    parentRegion: String,
    childRegion: String,
    latitude: Double,
    longitude: Double,
    status: WeatherStatus,
    val time: LocalDateTime,
    val data: HourlyWeatherData
): Weather(parentRegion, childRegion, latitude, longitude, status)

@Embeddable
data class HourlyWeatherData(
    @Column(name = "weather_code")
    val weatherCode: Int,
    val temperature2m: Double,
    val temperature80m: Double,
    val temperature120m: Double,
    val temperature180m: Double,
    val windSpeed10m: Double,
    val windSpeed80m: Double,
    val windSpeed120m: Double,
    val windSpeed180m: Double,
    val humidity2m: Int
)