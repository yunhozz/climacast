package com.climacast.subscription_service.model.dto.validator

import com.climacast.subscription_service.common.annotation.CustomValidator
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class SubscriptionRequestValidator : ConstraintValidator<CustomValidator, CustomValidatable> {

    override fun isValid(cv: CustomValidatable, ctx: ConstraintValidatorContext): Boolean {
        ctx.disableDefaultConstraintViolation()
        return cv.validate(ctx)
    }
}