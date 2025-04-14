package com.climacast.subscription_service.model.document

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "history_weather")
@JsonIgnoreProperties(ignoreUnknown = true)
data class HistoryWeather(
    val lat: Double,
    val lon: Double,
    @Id
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
) : WeatherDocument {

    override fun getId() = region

    override fun sliceByTime(startTime: String, endTime: String) = this.copy(
        time = time?.slice(time, startTime, endTime),
        weatherStatus = weatherStatus?.slice(time, startTime, endTime),
        maxTemperature2m = maxTemperature2m?.slice(time, startTime, endTime),
        minTemperature2m = minTemperature2m?.slice(time, startTime, endTime),
        maxApparentTemperature = maxApparentTemperature?.slice(time, startTime, endTime),
        minApparentTemperature = minApparentTemperature?.slice(time, startTime, endTime),
        sunrise = sunrise?.slice(time, startTime, endTime),
        sunset = sunset?.slice(time, startTime, endTime),
        daylightDuration = daylightDuration?.slice(time, startTime, endTime),
        sunshineDuration = sunshineDuration?.slice(time, startTime, endTime),
        maxUvIndex = maxUvIndex?.slice(time, startTime, endTime),
        maxUvIndexClearSky = maxUvIndexClearSky?.slice(time, startTime, endTime),
        precipitationSum = precipitationSum?.slice(time, startTime, endTime),
        rainSum = rainSum?.slice(time, startTime, endTime),
        showersSum = showersSum?.slice(time, startTime, endTime),
        snowfallSum = snowfallSum?.slice(time, startTime, endTime),
        precipitationHours = precipitationHours?.slice(time, startTime, endTime),
        maxPrecipitationProbability = maxPrecipitationProbability?.slice(time, startTime, endTime),
        maxWindSpeed10m = maxWindSpeed10m?.slice(time, startTime, endTime),
        maxWindGusts10m = maxWindGusts10m?.slice(time, startTime, endTime),
        windDirection10m = windDirection10m?.slice(time, startTime, endTime),
        sumOfShortwaveRadiation = sumOfShortwaveRadiation?.slice(time, startTime, endTime),
        refEvapotranspiration = refEvapotranspiration?.slice(time, startTime, endTime),
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