package com.climacast.batch_server.common.api

import com.climacast.batch_server.dto.Region
import com.climacast.global.dto.WeatherResponseDTO
import com.climacast.global.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.URI

@Component
class WeatherResponseProcessor(
    private val openMeteoWebClient: WebClient
) {
    @Value("\${open-api.open-meteo.end-point.weather-forecast}")
    private lateinit var weatherRequestEndPoint: String

    private val log = logger()

    companion object {
        const val TIMEZONE = "Asia/Tokyo"
    }

    fun sendHourlyWeatherRequest(region: Region, request: WeatherQueryRequest, hourly: String?): Mono<WeatherResponseDTO> =
        retrieveWeatherResponse(region, request, hourly = hourly)

    fun sendDailyWeatherRequest(region: Region, request: WeatherQueryRequest, daily: String?): Mono<WeatherResponseDTO> =
        retrieveWeatherResponse(region, request, daily = daily)

    private fun retrieveWeatherResponse(
        region: Region,
        request: WeatherQueryRequest,
        daily: String? = null,
        hourly: String? = null
    ): Mono<WeatherResponseDTO> {
        val (parentRegion, childRegion, latitude, longitude) = region
        return openMeteoWebClient.get()
            .uri { createWeatherRequestUri(it, latitude, longitude, request, daily, hourly) }
            .retrieve()
            .bodyToMono(WeatherResponseDTO::class.java)
            .map {
                it.apply {
                    this.parentRegion = parentRegion
                    this.childRegion = childRegion
                    this.weatherType = request.weatherType
                }
            }
            .doOnError { log.error("Failed to fetch weather data : ${it.localizedMessage}", it) }
    }

    private fun createWeatherRequestUri(
        builder: UriBuilder,
        latitude: Double,
        longitude: Double,
        query: WeatherQueryRequest,
        daily: String?,
        hourly: String?
    ): URI =
        builder.path(weatherRequestEndPoint)
            .queryParam("latitude", latitude)
            .queryParam("longitude", longitude)
            .queryParam("past_days", query.pastDays)
            .queryParam("forecast_days", query.forecastDays)
            .queryParam("timezone", TIMEZONE)
            .apply {
                daily?.let { queryParam("daily", it) }
                hourly?.let { queryParam("hourly", it) }
            }
            .build()
}