package com.climacast.ai_service.model.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class WeatherQueryRequestDTO(
    @field:NotNull(message = "날씨 종류를 입력해주세요.")
    val weatherType: String,

    @field:NotBlank(message = "상위 지역을 입력해주세요.")
    val parentRegion: String,

    @field:NotBlank(message = "하위 지역을 입력해주세요.")
    val childRegion: String,

    val isWholeTime: Boolean = false,
    val startTime: String? = null,
    val endTime: String? = null,

    @field:NotBlank(message = "요청 사항을 입력해주세요.")
    val message: String
) {
    @AssertTrue(message = "시작 및 종료 시간대를 입력해주세요.")
    fun isTimeNotNull(): Boolean =
        if (!isWholeTime) startTime != null && endTime != null
        else true
}