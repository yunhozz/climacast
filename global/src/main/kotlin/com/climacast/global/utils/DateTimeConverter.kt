package com.climacast.global.utils

import com.climacast.global.enums.DateTimePattern
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeConverter {
    inline fun <reified T> convertToLocalDateTime(datetime: T, pattern: DateTimePattern): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern(DateTimePattern.of(pattern))
        return LocalDateTime.parse(datetime.toString(), formatter)
    }

    inline fun <reified T> convertToString(datetime: T, pattern: DateTimePattern): String {
        val formatter = DateTimeFormatter.ofPattern(DateTimePattern.of(pattern))
        return LocalDateTime.parse(datetime.toString()).format(formatter)
    }
}