package com.climacast.ai_service.controller

import com.climacast.ai_service.dto.WeatherQueryRequestDTO
import com.climacast.ai_service.service.WeatherAiService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/weather")
class WeatherAiController(
    private val weatherAiService: WeatherAiService
) {
    @PostMapping("/test1")
    fun test1(@RequestBody message: String) = weatherAiService.test1(message)

    @PostMapping("/test2")
    fun test2() = weatherAiService.test2()

    @PostMapping("/query")
    fun queryWeatherInformation(@Valid @RequestBody dto: WeatherQueryRequestDTO) =
        weatherAiService.processQuery(dto)
}