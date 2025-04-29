package com.climacast.ai_service.common.exception

import com.climacast.global.enums.ApiResponseCode

sealed class AiServiceException(val errorCode: ApiResponseCode.ErrorCode) : RuntimeException(errorCode.message) {
    class KafkaSendException : AiServiceException(ApiResponseCode.ErrorCode.INTERNAL_SERVER_ERROR)
    class WeatherDataResponseTimeoutException : AiServiceException(ApiResponseCode.ErrorCode.INTERNAL_SERVER_ERROR)
}