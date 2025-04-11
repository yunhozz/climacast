package com.climacast.subscription_service.common.annotation

import com.climacast.subscription_service.model.dto.validator.SubscriptionValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [SubscriptionValidator::class])
annotation class CustomValidator(
    val message: String = "Invalid Request",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)