package com.climacast.subscription_service.controller

import com.climacast.global.dto.ApiResponse
import com.climacast.global.enums.ApiResponseCode
import com.climacast.subscription_service.model.dto.SubscriptionCreateRequestDTO
import com.climacast.subscription_service.model.dto.SubscriptionResponseDTO
import com.climacast.subscription_service.model.dto.SubscriptionUpdateRequestDTO
import com.climacast.subscription_service.service.SubscriptionCrudService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/subscription")
class SubscriptionController(
    private val subscriptionCrudService: SubscriptionCrudService
) {
    @GetMapping
    fun getSubscriptionInformation(
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) phoneNumber: String?
    ): ApiResponse<SubscriptionResponseDTO> {
        val subscription = subscriptionCrudService.getSubscriptionByEmailAndPhoneNumber(email, phoneNumber)
        return ApiResponse.success(
            ApiResponseCode.SuccessCode.SUBSCRIPTION_READ_SUCCESS,
            subscription
        )
    }

    @PostMapping
    fun registerSubscription(@Valid @RequestBody dto: SubscriptionCreateRequestDTO): ApiResponse<Long> {
        val subscriptionId = subscriptionCrudService.createSubscription(dto)
        return ApiResponse.success(
            ApiResponseCode.SuccessCode.SUBSCRIBE_CREATE_SUCCESS,
            subscriptionId
        )
    }

    @PatchMapping
    fun updateSubscription(@Valid @RequestBody dto: SubscriptionUpdateRequestDTO): ApiResponse<Long> {
        val subscriptionId = subscriptionCrudService.updateSubscription(dto)
        return ApiResponse.success(
            ApiResponseCode.SuccessCode.SUBSCRIBE_UPDATE_SUCCESS,
            subscriptionId
        )
    }

    @DeleteMapping
    fun cancelSubscription(
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) phoneNumber: String?
    ): ApiResponse<Void> {
        subscriptionCrudService.cancelSubscriptionByEmailAndPhoneNumber(email, phoneNumber)
        return ApiResponse.success(ApiResponseCode.SuccessCode.SUBSCRIBE_CANCELED)
    }
}