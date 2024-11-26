package com.climacast.batch_server.config.manager.api.parser

import com.climacast.batch_server.config.Region
import com.climacast.batch_server.config.manager.api.WeatherQueryRequest
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

    companion object {
        val log = logger()
        private const val TIMEZONE = "Asia/Tokyo"

        private fun createWeatherRequestUri(
            builder: UriBuilder,
            endPoint: String,
            latitude: Double,
            longitude: Double,
            daily: String?,
            hourly: String?,
            query: WeatherQueryRequest
        ): URI {
            val uri = builder.path(endPoint)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("past_days", query.pastDays)
                .queryParam("forecast_days", query.forecastDays)
                .queryParam("timezone", TIMEZONE)

            daily?.let { uri.queryParam("daily", it) }
                ?: hourly?.let { uri.queryParam("hourly", it) }

            return uri.build()
        }
    }

    override fun sendHourlyWeatherRequest(region: Region, query: WeatherQueryRequest, hourly: String?): Mono<WeatherResponseDTO> =
        retrieveWeatherResponse(region, query, null, hourly)

    override fun sendDailyWeatherRequest(region: Region, query: WeatherQueryRequest, daily: String?): Mono<WeatherResponseDTO> =
        retrieveWeatherResponse(region, query, daily, null)

    private fun retrieveWeatherResponse(
        region: Region,
        query: WeatherQueryRequest,
        daily: String?,
        hourly: String?,
    ): Mono<WeatherResponseDTO> {
        val (parentRegion, childRegion, latitude, longitude) = region
        return openMeteoWebClient.get()
            .uri { createWeatherRequestUri(it, weatherRequestEndPoint, latitude, longitude, daily, hourly, query) }
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
}