package com.climacast.batch_server.config.handler.impl

import com.climacast.batch_server.config.handler.WeatherQueryRequest
import com.climacast.batch_server.config.handler.WeatherResponseParser
import com.climacast.batch_server.dto.Region
import com.climacast.batch_server.dto.WeatherResponseDTO
import com.climacast.global.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.URI

@Component
class WeatherResponseParserImpl(
    private val openMeteoWebClient: WebClient
): WeatherResponseParser {

    @Value("\${open-api.open-meteo.end-point.weather-forecast}")
    private lateinit var weatherRequestEndPoint: String

    private val log = logger()

    companion object {
        private const val TIMEZONE = "Asia/Tokyo"
    }

    override fun sendHourlyWeatherRequest(region: Region, query: WeatherQueryRequest, hourly: String?): Mono<WeatherResponseDTO> =
        retrieveWeatherResponse(region, query, hourly = hourly)

    override fun sendDailyWeatherRequest(region: Region, query: WeatherQueryRequest, daily: String?): Mono<WeatherResponseDTO> =
        retrieveWeatherResponse(region, query, daily = daily)

    private fun retrieveWeatherResponse(
        region: Region,
        query: WeatherQueryRequest,
        daily: String? = null,
        hourly: String? = null
    ): Mono<WeatherResponseDTO> {
        val (parentRegion, childRegion, latitude, longitude) = region
        return openMeteoWebClient.get()
            .uri { createWeatherRequestUri(it, latitude, longitude, query, daily, hourly) }
            .retrieve()
            .bodyToMono(WeatherResponseDTO::class.java)
            .map {
                it.apply {
                    this.parentRegion = parentRegion
                    this.childRegion = childRegion
                    this.weatherType = query.weatherType
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