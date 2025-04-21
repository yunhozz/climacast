package com.climacast.global.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeConverter {
    val DATETIME_FORMATTER_1: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH시 mm분")
    val DATETIME_FORMATTER_2: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

    inline fun <reified T> convertToLocalDateTime(datetime: T): LocalDateTime = LocalDateTime.parse(datetime.toString(), DATETIME_FORMATTER_1)
    inline fun <reified T> parseToLocalDateTime(datetime: T): LocalDateTime = LocalDateTime.parse(datetime.toString(), DATETIME_FORMATTER_2)
}