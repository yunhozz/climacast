package com.climacast.subscription_service.common.exception

import com.climacast.global.dto.ApiResponse
import com.climacast.global.dto.ApiResponse.ErrorData
import com.climacast.global.enums.ApiResponseCode
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class SubscriptionServiceExceptionHandler {

    @ExceptionHandler(SubscriptionServiceException::class)
    fun handleSubscriptionServiceException(e: SubscriptionServiceException): ApiResponse<ErrorData> {
        val errorData = ErrorData.of(e.localizedMessage)
        return ApiResponse.fail(e.errorCode, errorData)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ApiResponse<ErrorData> {
        val bindingResult = e.bindingResult
        val errorData = ErrorData.of(
            bindingResult.fieldErrors.map { it.defaultMessage }.toString(),
            ErrorData.FieldError.of(bindingResult)
        )

        return ApiResponse.fail(ApiResponseCode.ErrorCode.BAD_REQUEST, errorData)
    }
}