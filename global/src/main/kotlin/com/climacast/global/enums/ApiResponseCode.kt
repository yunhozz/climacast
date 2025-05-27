package com.climacast.global.enums

sealed interface ApiResponseCode {
    enum class SuccessCode(
        val statusCode: Int,
        val message: String
    ) : ApiResponseCode {
        // 200 OK
        SUBSCRIPTION_READ_SUCCESS(OK, "구독 정보 조회 성공"),

        // 201 Created
        SUBSCRIBE_CREATE_SUCCESS(CREATED, "구독 성공"),
        SUBSCRIBE_UPDATE_SUCCESS(CREATED, "구독 정보 수정 성공"),
        AI_RESPONSE_SUCCESS(CREATED, "AI 응답 성공"),

        // 204 No Content
        SUBSCRIBE_CANCELED(NO_CONTENT, "구독 취소 성공")
    }

    enum class ErrorCode(
        val statusCode: Int,
        val message: String
    ) : ApiResponseCode {
        // 400 Bad Request
        BAD_REQUEST(ApiResponseCode.BAD_REQUEST, "잘못된 요청입니다."),

        // 401 Unauthorized
        UNAUTHORIZED(ApiResponseCode.UNAUTHORIZED, "인증되지 않은 사용자입니다."),

        // 403 Forbidden
        FORBIDDEN(ApiResponseCode.FORBIDDEN, "권한이 없습니다."),

        // 404 Not Found
        WEATHER_DOCUMENT_NOT_FOUND(NOT_FOUND, "해당 지역에 대한 날씨 document 를 찾을 수 없습니다."),
        SUBSCRIPTION_NOT_FOUND(NOT_FOUND, "구독 정보를 찾을 수 없습니다."),

        // 405 Method Not Allowed
        METHOD_NOT_ALLOWED(ApiResponseCode.METHOD_NOT_ALLOWED, "허용되지 않은 메소드입니다."),

        // 409 Conflict
        SUBSCRIPTION_ALREADY_EXIST(CONFLICT, "구독 정보가 이미 존재합니다."),
        SUBSCRIPTION_ALREADY_CANCELED(CONFLICT, "이미 취소된 구독입니다."),

        // 500 Internal Server Error
        INTERNAL_SERVER_ERROR(ApiResponseCode.INTERNAL_SERVER_ERROR, "서버에 오류가 발생하였습니다.")
    }

    companion object {
        const val OK = 200
        const val CREATED = 201
        const val NO_CONTENT = 204
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val METHOD_NOT_ALLOWED = 405
        const val CONFLICT = 409
        const val INTERNAL_SERVER_ERROR = 500
    }
}