package com.climacast.ai_service.controller

import com.climacast.ai_service.model.dto.WeatherQueryRequestDTO
import com.climacast.ai_service.service.WeatherAiService
import com.climacast.global.dto.ApiResponse
import com.climacast.global.enums.ApiResponseCode
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/ai/weather")
class WeatherAiController(
    private val weatherAiService: WeatherAiService
) {
    @PostMapping("/query")
    fun queryWeatherInformation(@Valid @RequestBody dto: WeatherQueryRequestDTO): Mono<ApiResponse<String>> =
        weatherAiService.processQuery(dto)
            .flatMap { response ->
                val apiResponse = ApiResponse.success(
                    ApiResponseCode.SuccessCode.AI_RESPONSE_SUCCESS,
                    response
                )
                Mono.just(apiResponse)
            }
}