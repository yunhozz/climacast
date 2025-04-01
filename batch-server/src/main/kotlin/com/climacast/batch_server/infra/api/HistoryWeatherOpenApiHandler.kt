package com.climacast.batch_server.infra.api

import com.climacast.batch_server.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.dto.Region
import com.climacast.global.dto.WeatherResponseDTO
import com.climacast.global.enums.WeatherType
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class HistoryWeatherOpenApiHandler(
    private val weatherApiProcessor: WeatherApiProcessor
) : OpenApiHandler<Region, WeatherResponseDTO>() {

    override fun callOpenApi(chunk: Chunk<out Region>, query: Any): List<WeatherResponseDTO> {
        val (hourlyValues, dailyValues, pastDays, _) = query as OpenApiQueryRequestDTO
        val hourly = hourlyValues?.joinToString(",")
        val daily = dailyValues?.joinToString(",")
        val req = WeatherQueryRequest(pastDays, forecastDays = 0, weatherType = WeatherType.HISTORY)

        return Mono.zip(
            Mono.fromCallable {
                callWeatherOpenApi(chunk) { region ->
                    weatherApiProcessor.sendHourlyWeatherRequest(region, req, hourly)
                }
            },
            Mono.fromCallable {
                callWeatherOpenApi(chunk) { region ->
                    weatherApiProcessor.sendDailyWeatherRequest(region, req, daily)
                }
            })
            .map { tuple -> tuple.t1!! + tuple.t2!! }
            .block() ?: throw IllegalArgumentException("There are no historical weather data")
    }
}