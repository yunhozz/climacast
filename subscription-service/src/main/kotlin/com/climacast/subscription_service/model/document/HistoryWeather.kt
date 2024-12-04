package com.climacast.subscription_service.model.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "history_weather", createIndex = true)
data class HistoryWeather(
    @Id
    val id: String,
    val lat: Double,
    val lon: Double,
    val region: String,
    val time: List<String>?,
    val weatherStatus: List<String>?,
    val maxTemperature2m: List<Double>?,
    val minTemperature2m: List<Double>?,
    val maxApparentTemperature: List<Double>?,
    val minApparentTemperature: List<Double>?,
    val sunrise: List<String>?,
    val sunset: List<String>?,
    val daylightDuration: List<Double>?,
    val sunshineDuration: List<Double>?,
    val maxUvIndex: List<Double>?,
    val maxUvIndexClearSky: List<Double>?,
    val precipitationSum: List<Double>?,
    val rainSum: List<Double>?,
    val showersSum: List<Double>?,
    val snowfallSum: List<Double>?,
    val precipitationHours: List<Double>?,
    val maxPrecipitationProbability: List<Double>?,
    val maxWindSpeed10m: List<Double>?,
    val maxWindGusts10m: List<Double>?,
    val windDirection10m: List<Int>?,
    val sumOfShortwaveRadiation: List<Double>?,
    val refEvapotranspiration: List<Double>?,
    val temperature2m: List<Double>?,
    val temperature80m: List<Double>?,
    val temperature120m: List<Double>?,
    val temperature180m: List<Double>?,
    val windSpeed10m: List<Double>?,
    val windSpeed80m: List<Double>?,
    val windSpeed120m: List<Double>?,
    val windSpeed180m: List<Double>?,
    val humidity2m: List<Int>?
)