package com.climacast.subscription_service.model.document

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "forecast_weather")
@JsonIgnoreProperties(ignoreUnknown = true)
data class ForecastWeather(
    val lat: Double,
    val lon: Double,
    @Id
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
): WeatherDocument {
    override fun getId() = region
}