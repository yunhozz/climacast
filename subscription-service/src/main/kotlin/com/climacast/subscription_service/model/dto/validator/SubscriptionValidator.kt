package com.climacast.subscription_service.model.dto.validator

import com.climacast.subscription_service.common.annotation.CustomValidator
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.model.dto.SubscriptionCreateRequestDTO
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class SubscriptionValidator : ConstraintValidator<CustomValidator, SubscriptionCreateRequestDTO> {

    override fun isValid(dto: SubscriptionCreateRequestDTO, ctx: ConstraintValidatorContext): Boolean {
        val subscriptionMethod = SubscriptionMethod.of(dto.method)
        val email = dto.email
        val phoneNumber = dto.phoneNumber

        ctx.disableDefaultConstraintViolation()

        return when (subscriptionMethod) {
            SubscriptionMethod.MAIL, SubscriptionMethod.SLACK -> isEmailValid(email, ctx)
            SubscriptionMethod.SMS -> isPhoneNumberValid(phoneNumber, ctx)
        }
    }

    private fun isEmailValid(email: String?, ctx: ConstraintValidatorContext): Boolean {
        if (email.isNullOrBlank()) {
            ctx.buildConstraintViolationWithTemplate("이메일을 입력해주세요.")
                .addPropertyNode("email")
                .addConstraintViolation()
            return false
        } else if (!EMAIL_REGEX.matches(email)) {
            ctx.buildConstraintViolationWithTemplate("정확한 형식의 이메일을 입력해주세요.")
                .addPropertyNode("email")
                .addConstraintViolation()
            return false
        }
        return true
    }

    private fun isPhoneNumberValid(phoneNumber: String?, ctx: ConstraintValidatorContext): Boolean =
        if (phoneNumber.isNullOrBlank()) {
            ctx.buildConstraintViolationWithTemplate("전화번호를 입력해주세요.")
                .addPropertyNode("phoneNumber")
                .addConstraintViolation()
            false
        } else true

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}