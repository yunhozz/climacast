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
) : WeatherDocument {

    override fun getId() = region

    override fun sliceByTime(startTime: String, endTime: String) = this.copy(
        time = time?.slice(time, startTime, endTime),
        weatherStatus = weatherStatus?.slice(time, startTime, endTime),
        temperature2m = temperature2m?.slice(time, startTime, endTime),
        temperature80m = temperature80m?.slice(time, startTime, endTime),
        temperature120m = temperature120m?.slice(time, startTime, endTime),
        temperature180m = temperature180m?.slice(time, startTime, endTime),
        windSpeed10m = windSpeed10m?.slice(time, startTime, endTime),
        windSpeed80m = windSpeed80m?.slice(time, startTime, endTime),
        windSpeed120m = windSpeed120m?.slice(time, startTime, endTime),
        windSpeed180m = windSpeed180m?.slice(time, startTime, endTime),
        humidity2m = humidity2m?.slice(time, startTime, endTime)
    )
}