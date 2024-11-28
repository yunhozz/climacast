package com.climacast.batch_server.config.handler.impl

import com.climacast.batch_server.common.enums.WeatherType
import com.climacast.batch_server.config.handler.OpenApiHandler
import com.climacast.batch_server.config.handler.WeatherQueryRequest
import com.climacast.batch_server.config.handler.WeatherResponseParser
import com.climacast.batch_server.dto.WeatherResponseDTO
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class OpenApiHandlerImpl(
    weatherResponseParser: WeatherResponseParser
): OpenApiHandler(weatherResponseParser) {

    // 매 시간마다 실행
    override fun callForecastWeatherOpenApi(): List<WeatherResponseDTO> {
        val (hourlyValues, _, _, forecastDays) = dto
        val hourly = hourlyValues?.joinToString(",")
        val query = WeatherQueryRequest(pastDays = 0, forecastDays, weatherType = WeatherType.FORECAST)

        return callWeatherOpenApi { region ->
            weatherResponseParser.sendHourlyWeatherRequest(region, query, hourly)
        } ?: throw IllegalArgumentException("There are no forecast weather data")
    }

    // 매일 0시에 실행
    override fun callHistoricalWeatherOpenApi(): List<WeatherResponseDTO> {
        val (hourlyValues, dailyValues, pastDays, _) = dto
        val daily = dailyValues?.joinToString(",")
        val hourly = hourlyValues?.joinToString(",")
        val query = WeatherQueryRequest(pastDays, forecastDays = 0, weatherType = WeatherType.HISTORY)

        return Mono.zip(
            Mono.fromCallable {
                callWeatherOpenApi { region ->
                    weatherResponseParser.sendHourlyWeatherRequest(region, query, hourly)
                }
            },
            Mono.fromCallable {
                callWeatherOpenApi { region ->
                    weatherResponseParser.sendDailyWeatherRequest(region, query, daily)
                }
            })
            .map { tuple -> tuple.t1!! + tuple.t2!! }
            .block() ?: throw IllegalArgumentException("There are no historical weather data")
    }
}