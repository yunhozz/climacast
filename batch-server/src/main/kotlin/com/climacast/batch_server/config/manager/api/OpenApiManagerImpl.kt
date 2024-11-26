package com.climacast.batch_server.config.manager.api

import com.climacast.batch_server.common.enums.WeatherType
import com.climacast.batch_server.config.Region
import com.climacast.batch_server.config.manager.api.parser.WeatherResponseParser
import com.climacast.batch_server.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.dto.WeatherResponseDTO
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class OpenApiManagerImpl(
    private val weatherResponseParser: WeatherResponseParser
): OpenApiManager {

    // 매 시간마다 실행
    override fun callForecastWeatherOpenApi(regions: Set<Region>, dto: OpenApiQueryRequestDTO): MutableList<WeatherResponseDTO>? {
        val (hourlyValues, _, _, forecastDays) = dto
        val hourly = hourlyValues?.joinToString(",")
        val query = WeatherQueryRequest(pastDays = 0, forecastDays, weatherType = WeatherType.FORECAST)

        return Flux.fromIterable(regions)
            .flatMap { region ->
                weatherResponseParser.sendHourlyWeatherRequest(region, query, hourly)
            }
            .collectList()
            .block()
    }

    // 매일 0시에 실행
    override fun callHistoricalWeatherOpenApi(regions: Set<Region>, dto: OpenApiQueryRequestDTO): MutableList<WeatherResponseDTO>? {
        val (hourlyValues, dailyValues, pastDays, _) = dto
        val daily = dailyValues?.joinToString(",")
        val hourly = hourlyValues?.joinToString(",")
        val query = WeatherQueryRequest(pastDays, forecastDays = 0, weatherType = WeatherType.HISTORY)

        return Flux.fromIterable(regions)
            .flatMap({ region ->
                Flux.merge(
                    weatherResponseParser.sendDailyWeatherRequest(region, query, daily),
                    weatherResponseParser.sendHourlyWeatherRequest(region, query, hourly)
                )
            }, 30)
            .collectList()
            .block()
    }
}