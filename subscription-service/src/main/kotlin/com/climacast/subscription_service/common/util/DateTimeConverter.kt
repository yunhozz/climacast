package com.climacast.subscription_service.common.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeConverter {
    private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH시 mm분")

    fun parseDateTime(datetimeStr: String): LocalDateTime = LocalDateTime.parse(datetimeStr, DATETIME_FORMATTER)
    fun convertTimeFormat(datetimeStr: String): String = LocalDateTime.parse(datetimeStr).format(DATETIME_FORMATTER)
}