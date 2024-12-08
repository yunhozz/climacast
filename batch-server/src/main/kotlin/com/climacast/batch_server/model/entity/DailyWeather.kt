package com.climacast.batch_server.model.entity

import com.climacast.global.enums.WeatherStatus
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
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
    status: WeatherStatus,
    time: LocalDate,
    @Embedded
    val data: DailyWeatherData
): Weather(parentRegion, childRegion, latitude, longitude, status, time.toString())

@Embeddable
data class DailyWeatherData(
    val weatherCode: Int?,
    val maxTemperature2m: Double?,
    val minTemperature2m: Double?,
    val maxApparentTemperature: Double?,
    val minApparentTemperature: Double?,
    val sunrise: LocalDateTime?,
    val sunset: LocalDateTime?,
    val daylightDuration: Double?,
    val sunshineDuration: Double?,
    val maxUvIndex: Double?,
    val maxUvIndexClearSky: Double?,
    val precipitationSum: Double?,
    val rainSum: Double?,
    val showersSum: Double?,
    val snowfallSum: Double?,
    val precipitationHours: Double?,
    val maxPrecipitationProbability: Double?,
    val maxWindSpeed10m: Double?,
    val maxWindGusts10m: Double?,
    val windDirection10m: Int?,
    val sumOfShortwaveRadiation: Double?,
    val refEvapotranspiration: Double?
)