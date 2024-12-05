package com.climacast.subscription_service.model.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import java.util.UUID

@Document(indexName = "forecast_weather", createIndex = true)
data class ForecastWeather(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val lat: Double,
    val lon: Double,
    val region: String,
    val time: List<String>?,
    val weatherStatus: List<String>?,
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