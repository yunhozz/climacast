package com.climacast.subscription_service.model.dto.validator

import jakarta.validation.ConstraintValidatorContext

abstract class CustomValidatable {
    abstract fun validate(ctx: ConstraintValidatorContext): Boolean

    protected fun isEmailValid(email: String?, ctx: ConstraintValidatorContext): Boolean {
        if (email.isNullOrBlank()) {
            ctx.buildConstraintViolationWithTemplate("이메일을 입력해주세요.")
                .addPropertyNode("email")
                .addConstraintViolation()
            return false

        } else if (!EMAIL_REGEX.matches(email)) {
            ctx.buildConstraintViolationWithTemplate("이메일 형식에 맞지 않습니다.")
                .addPropertyNode("email")
                .addConstraintViolation()
            return false

        }
        return true
    }

    protected fun isPhoneNumberValid(phoneNumber: String?, ctx: ConstraintValidatorContext): Boolean =
        if (phoneNumber.isNullOrBlank()) {
            ctx.buildConstraintViolationWithTemplate("전화번호를 입력해주세요.")
                .addPropertyNode("phoneNumber")
                .addConstraintViolation()
            false
        } else true

    companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}