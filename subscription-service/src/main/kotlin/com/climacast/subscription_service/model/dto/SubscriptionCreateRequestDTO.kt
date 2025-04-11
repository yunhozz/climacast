package com.climacast.subscription_service.model.dto

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.annotation.CustomValidator
import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.model.entity.Subscription
import com.climacast.subscription_service.model.entity.SubscriptionInfo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@CustomValidator
data class SubscriptionCreateRequestDTO(
    val email: String?,
    val phoneNumber: String?,

    @field:NotEmpty(message = "지역을 선택해주세요.")
    @field:Size(min = 1, max = 5, message = "1개 이상 5개 이하의 지역을 입력해주세요.")
    val regions: Set<String>,

    @field:NotBlank(message = "날씨 예보/히스토리 중 하나를 선택해주세요.")
    val weatherType: String,

    @field:NotBlank(message = "구독 주기를 입력해주세요.")
    val interval: String,

    @field:NotBlank(message = "구독 방식을 입력해주세요.")
    val method: String
) {
    fun toEntity() = Subscription(
        SubscriptionInfo(email, phoneNumber),
        regions,
        WeatherType.of(weatherType),
        SubscriptionInterval.of(interval),
        SubscriptionMethod.of(method)
    )
}