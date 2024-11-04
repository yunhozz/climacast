package com.climacast.batch_server.config.manager.impl

import com.climacast.batch_server.config.manager.OpenApiManager
import com.climacast.batch_server.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.dto.WeatherResponseDTO
import com.climacast.global.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Component
class OpenApiManagerImpl(
    private val openMeteoWebClient: WebClient
): OpenApiManager {

    private val log = logger()

    @Value("\${open-api.open-meteo.end-point.weather-forecast}")
    private lateinit var weatherForecastEndPoint: String

    // 매 시간마다 실행
    override fun callForecastWeatherOpenApi(cityInfoList: List<String>, dto: OpenApiQueryRequestDTO): MutableList<WeatherResponseDTO>? {
        val (hourlyValues, _, _, forecastDays) = dto
        val hourly = hourlyValues.joinToString(",")

        return Flux.fromIterable(cityInfoList)
            .flatMap { cityInfo ->
                val parts = cityInfo.split(",")
                val (parentRegion, childRegion, latitude, longitude) = parts

                openMeteoWebClient.get()
                    .uri { uriBuilder ->
                        uriBuilder.path(weatherForecastEndPoint)
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("hourly", hourly)
                            .queryParam("past_days", 0)
                            .queryParam("forecast_days", forecastDays)
                            .queryParam("timezone", "Asia/Tokyo")
                            .build()
                    }
                    .retrieve()
                    .bodyToMono(WeatherResponseDTO::class.java)
                    .doOnNext {
                        it.parentRegion = parentRegion
                        it.childRegion = childRegion
                    }
                    .doOnError { ex -> log.error(ex.localizedMessage) }
            }
            .collectList()
            .block()
    }

    // 매일 0시에 실행
    override fun callHistoricalWeatherOpenApi(cityInfoList: List<String>, dto: OpenApiQueryRequestDTO): MutableList<WeatherResponseDTO>? {
        val (hourlyValues, dailyValues, pastDays, _) = dto
        val hourly = hourlyValues.joinToString(",")
        val daily = dailyValues.joinToString(",")

        return Flux.fromIterable(cityInfoList)
            .flatMap { cityInfo ->
                val parts = cityInfo.split(",")
                val (parentRegion, childRegion, latitude, longitude) = parts

                openMeteoWebClient.get()
                    .uri { uriBuilder ->
                        uriBuilder.path(weatherForecastEndPoint)
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("hourly", hourly)
                            .queryParam("daily", daily)
                            .queryParam("past_days", pastDays)
                            .queryParam("forecast_days", 0)
                            .queryParam("timezone", "Asia/Tokyo")
                            .build()
                    }
                    .retrieve()
                    .bodyToMono(WeatherResponseDTO::class.java)
                    .doOnNext {
                        it.parentRegion = parentRegion
                        it.childRegion = childRegion
                    }
                    .doOnError { ex -> log.error(ex.localizedMessage) }
            }
            .collectList()
            .block()
    }
}