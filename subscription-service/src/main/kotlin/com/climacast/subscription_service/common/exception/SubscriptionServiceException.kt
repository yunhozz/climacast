package com.climacast.subscription_service.common.exception

import com.climacast.global.enums.ApiResponseCode

sealed class SubscriptionServiceException(val errorCode: ApiResponseCode.ErrorCode) : RuntimeException(errorCode.message) {
    class SubscriptionNotFoundException : SubscriptionServiceException(ApiResponseCode.ErrorCode.SUBSCRIPTION_NOT_FOUND)
    class SubscriptionAlreadyExistException : SubscriptionServiceException(ApiResponseCode.ErrorCode.SUBSCRIPTION_ALREADY_EXIST)
    class SubscriptionAlreadyCanceledException : SubscriptionServiceException(ApiResponseCode.ErrorCode.SUBSCRIPTION_ALREADY_CANCELED)
    class WeatherDocumentNotFoundException : SubscriptionServiceException(ApiResponseCode.ErrorCode.WEATHER_DOCUMENT_NOT_FOUND)
    class ElasticsearchClientException : SubscriptionServiceException(ApiResponseCode.ErrorCode.INTERNAL_SERVER_ERROR)
    class WeatherDataSendFailException : SubscriptionServiceException(ApiResponseCode.ErrorCode.INTERNAL_SERVER_ERROR)
    class ChromeWebDriverException : SubscriptionServiceException(ApiResponseCode.ErrorCode.INTERNAL_SERVER_ERROR)
}