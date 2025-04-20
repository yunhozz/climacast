package com.climacast.global.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeConverter {
    private val DATETIME_FORMATTER_1 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH시 mm분")
    private val DATETIME_FORMATTER_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

    fun convertToLocalDateTime(datetimeStr: String): LocalDateTime = LocalDateTime.parse(datetimeStr, DATETIME_FORMATTER_1)
    fun parseToLocalDateTime(datetimeStr: String?): LocalDateTime = LocalDateTime.parse(datetimeStr!!, DATETIME_FORMATTER_2)
}