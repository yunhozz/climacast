package com.climacast.subscription_service.model.dto

import com.climacast.subscription_service.common.annotation.CustomValidator
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.model.dto.validator.CustomValidatable
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@CustomValidator
data class SubscriptionUpdateRequestDTO(
    val email: String?,
    val phoneNumber: String?,

    @field:Size(min = 1, max = 5, message = "1개 이상 5개 이하의 지역을 입력해주세요.")
    val regions: Set<String>,

    @field:NotBlank(message = "구독 주기를 입력해주세요.")
    val interval: String,

    @field:NotBlank(message = "구독 방식을 입력해주세요.")
    val method: String
) : CustomValidatable {

    override fun validate(ctx: ConstraintValidatorContext): Boolean =
        when (SubscriptionMethod.of(method)) {
            SubscriptionMethod.MAIL, SubscriptionMethod.SLACK -> isEmailValid(email, ctx)
            SubscriptionMethod.SMS -> isPhoneNumberValid(phoneNumber, ctx)
        }
}