package com.climacast.ai_service.common.exception

import com.climacast.global.dto.ApiResponse
import com.climacast.global.dto.ApiResponse.ErrorData
import com.climacast.global.enums.ApiResponseCode
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import reactor.core.publisher.Mono

@RestControllerAdvice
class AiServiceExceptionHandler {

    @ExceptionHandler(AiServiceException::class)
    fun handleAiServiceException(e: AiServiceException): Mono<ApiResponse<ErrorData>> {
        val errorData = ErrorData.of(e.localizedMessage)
        val apiResponse = ApiResponse.fail(e.errorCode, errorData)
        return Mono.just(apiResponse)
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleMethodArgumentNotValidException(e: WebExchangeBindException): Mono<ApiResponse<ErrorData>> {
        val bindingResult = e.bindingResult
        val errorData = ErrorData.of(
            bindingResult.fieldErrors.map { it.defaultMessage }.toString(),
            ErrorData.FieldError.of(bindingResult)
        )
        val apiResponse = ApiResponse.fail(ApiResponseCode.ErrorCode.BAD_REQUEST, errorData)

        return Mono.just(apiResponse)
    }
}