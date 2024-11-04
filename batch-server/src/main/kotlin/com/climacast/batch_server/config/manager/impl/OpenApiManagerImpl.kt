package com.climacast.batch_server.config.manager.impl

import com.climacast.batch_server.config.manager.OpenApiManager
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

    override fun callOpenMeteoApi(cityInfoList: List<String>): MutableList<WeatherResponseDTO>? =
        Flux.fromIterable(cityInfoList)
            .flatMap { cityInfo ->
                val parts = cityInfo.split(",")
                val (parentRegion, childRegion, latitude, longitude) = parts

                openMeteoWebClient.get()
                    .uri { uriBuilder ->
                        uriBuilder.path(weatherForecastEndPoint)
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("hourly", "temperature_2m")
                            .queryParam("past_days", 1)
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