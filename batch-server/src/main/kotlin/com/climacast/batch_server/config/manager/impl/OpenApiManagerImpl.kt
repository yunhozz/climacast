package com.climacast.batch_server.config.manager.impl

import com.climacast.batch_server.config.manager.OpenApiManager
import com.climacast.batch_server.dto.GeocodingResponseDTO
import com.climacast.batch_server.dto.WeatherResponseDTO
import com.climacast.batch_server.model.document.Weather
import com.climacast.batch_server.model.document.WeatherData
import com.climacast.batch_server.model.repository.WeatherDocumentRepository
import com.climacast.common.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.nio.file.Files
import java.util.UUID

@Component
class OpenApiManagerImpl(
    private val openWeatherWebClient: WebClient,
    private val openMeteoWebClient: WebClient,
    private val weatherDocumentRepository: WeatherDocumentRepository
): OpenApiManager {

    companion object {
        const val CSV_PATH = "/static/region-list.csv"
    }

    private val log = logger()

    @Value("\${open-api.open-weather.api-key}")
    private lateinit var openWeatherApiKey: String

    @Value("\${open-api.open-weather.end-point.geocoding}")
    private lateinit var geocodingEndPoint: String

    @Value("\${open-api.open-meteo.end-point.weather-forecast}")
    private lateinit var weatherForecastEndPoint: String

    override fun callWeatherInfoByMySite(city: String): Mono<WeatherResponseDTO> =
        openWeatherWebClient.get()
            .uri {
                it.path(geocodingEndPoint)
                    .queryParam("appid", openWeatherApiKey)
                    .queryParam("limit", "5")
                    .queryParam("q", city)
                    .build()
            }
            .retrieve()
            .bodyToMono(object: ParameterizedTypeReference<List<GeocodingResponseDTO>>() {})
            .flatMap { responses ->
                responses.firstOrNull()?.let { response ->
                    openMeteoWebClient.get()
                        .uri {
                            it.path(weatherForecastEndPoint)
                                .queryParam("latitude", response.lat)
                                .queryParam("longitude", response.lon)
                                .queryParam("hourly", "temperature_2m")
                                .queryParam("timezone", "Asia/Tokyo")
                                .build()
                        }
                        .retrieve()
                        .bodyToMono(WeatherResponseDTO::class.java)
                } ?: run {
                    Mono.error(RuntimeException("Response data is null"))
                }
            }
            .doOnError {
                log.error(it.localizedMessage)
            }

    override fun saveWeatherHistoryOneDayAgo() {
        val resource = ClassPathResource(CSV_PATH)
        val lines = Files.lines(resource.file.toPath()).toList()

        Flux.fromIterable(lines)
            .flatMap { line ->
                val parts = line.split(",")
                val parentRegion = parts[0]
                val childRegion = parts[1]
                val latitude = parts[2].toDouble()
                val longitude = parts[3].toDouble()

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
                    .doOnError { ex -> log.error(ex.localizedMessage) }
                    .flatMapMany { response ->
                        val hourly = response.hourly
                        val weathers = hourly.time.zip(hourly.temperature_2m) { time, temperature ->
                            Weather(
                                UUID.randomUUID().toString(),
                                parentRegion,
                                childRegion,
                                latitude,
                                longitude,
                                time,
                                WeatherData(temperature)
                            )
                        }
                        Flux.fromIterable(weathers)
                    }
            }
            .doOnError { ex -> log.error(ex.localizedMessage) }
            .collectList()
            .flatMap { weathers ->
                Mono.fromCallable {
                    weatherDocumentRepository.saveAll(weathers)
                }.subscribeOn(Schedulers.boundedElastic()) // CPU 코어 수 x 10 만큼의 스레드 생성 -> Blocking I/O
            }
            .subscribe()
    }
}