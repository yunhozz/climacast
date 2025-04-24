package com.climacast.ai_service.model.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class WeatherQueryRequestDTO(
    @field:NotBlank(message = "날씨 종류를 입력해주세요.")
    val weatherType: String,

    @field:NotBlank(message = "상위 지역을 입력해주세요.")
    val parentRegion: String,

    @field:NotBlank(message = "하위 지역을 입력해주세요.")
    val childRegion: String,

    @field:NotNull(message = "전체 날짜에 대한 쿼리 여부를 선택해주세요.")
    val isWholeTime: Boolean?,

    val startTime: String?,
    val endTime: String?,
    val message: String?
) {
    @AssertTrue(message = "시작 및 종료 시간대를 다시 한번 확인해주세요.")
    fun isValidAboutTimes(): Boolean = isWholeTime?.let {
        if (it) startTime.isNullOrBlank() && endTime.isNullOrBlank()
        else !startTime.isNullOrBlank() && !endTime.isNullOrBlank()
    } ?: true
}