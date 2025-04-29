package com.climacast.subscription_service.model.dto.validator

import jakarta.validation.ConstraintValidatorContext

interface CustomValidatable {
    fun validate(ctx: ConstraintValidatorContext): Boolean

    fun isEmailValid(email: String?, ctx: ConstraintValidatorContext): Boolean =
        when (email.isNullOrBlank()) {
            true -> {
                ctx.buildConstraintViolationWithTemplate("이메일을 입력해주세요.")
                    .addPropertyNode("email")
                    .addConstraintViolation()
                false
            }
            false -> {
                if (!EMAIL_REGEX.matches(email)) {
                    ctx.buildConstraintViolationWithTemplate("이메일 형식에 맞지 않습니다.")
                        .addPropertyNode("email")
                        .addConstraintViolation()
                    false
                } else true
            }
        }

    fun isPhoneNumberValid(phoneNumber: String?, ctx: ConstraintValidatorContext): Boolean =
        when (phoneNumber.isNullOrBlank()) {
            true -> {
                ctx.buildConstraintViolationWithTemplate("전화번호를 입력해주세요.")
                    .addPropertyNode("phoneNumber")
                    .addConstraintViolation()
                false
            }
            false -> true
        }

    companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}