package com.climacast.ai_service.controller

import com.climacast.ai_service.common.enums.SessionType
import com.climacast.ai_service.common.util.SessionManager
import com.climacast.ai_service.model.dto.WeatherQueryRequestDTO
import com.climacast.ai_service.service.WeatherAiService
import com.climacast.global.dto.ApiResponse
import com.climacast.global.enums.ApiResponseCode
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/ai/weather")
class WeatherAiController(
    private val weatherAiService: WeatherAiService
) {
    @PostMapping("/analyze")
    fun analyzeWeatherSummary(@Valid @RequestBody dto: WeatherQueryRequestDTO, exchange: ServerWebExchange): Mono<ApiResponse<String>> =
        SessionManager.createSession(exchange.session, SessionType.WEATHER_AI_QUERY_SESSION)
            .flatMap { sessionId ->
                weatherAiService.processQuery(dto, sessionId)
                    .map { response ->
                        ApiResponse.success(
                            ApiResponseCode.SuccessCode.AI_RESPONSE_SUCCESS,
                            response
                        )
                    }
            }

    @PostMapping("/analyze/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamWeatherAnalysis(@Valid @RequestBody dto: WeatherQueryRequestDTO): Flux<String> =
        weatherAiService.processQueryStream(dto)
}