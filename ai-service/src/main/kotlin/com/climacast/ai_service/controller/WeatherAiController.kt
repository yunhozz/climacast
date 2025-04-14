package com.climacast.ai_service.controller

import com.climacast.ai_service.model.dto.WeatherQueryRequestDTO
import com.climacast.ai_service.service.WeatherAiService
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
    fun queryWeatherInformation(@Valid @RequestBody dto: WeatherQueryRequestDTO): Mono<String> =
        weatherAiService.processQuery(dto)
}