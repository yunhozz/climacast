package com.climacast.batch_server.config.manager.impl

import com.climacast.batch_server.config.manager.OpenApiManager
import com.climacast.batch_server.dto.WeatherResponseDTO
import com.climacast.batch_server.model.document.Weather
import com.climacast.batch_server.model.document.WeatherData
import com.climacast.batch_server.model.repository.WeatherDocumentRepository
import com.climacast.common.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.UUID

@Component
class OpenApiManagerImpl(
    private val openMeteoWebClient: WebClient,
    private val weatherDocumentRepository: WeatherDocumentRepository
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
                            .queryParam("timezone", "Asia/Tokyo")
                            .queryParam("past_days", "1")
                            .queryParam("forecast_days", "0")
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

    override fun saveWeathers(responses: List<WeatherResponseDTO>) {
        Flux.fromIterable(responses)
            .flatMap { response ->
                val hourly = response.hourly
                val weathers = hourly.time.zip(hourly.temperature_2m) { time, temperature ->
                    Weather(
                        UUID.randomUUID().toString(),
                        response.parentRegion,
                        response.childRegion,
                        response.latitude,
                        response.longitude,
                        time,
                        WeatherData(temperature)
                    )
                }
                Flux.fromIterable(weathers)
            }
            .collectList()
            .flatMap { weathers ->
                Mono.fromCallable {
                    weatherDocumentRepository.saveAll(weathers)
                }.subscribeOn(Schedulers.boundedElastic())
            }
            .subscribe()
    }
}