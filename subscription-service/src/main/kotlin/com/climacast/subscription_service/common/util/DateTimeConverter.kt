package com.climacast.subscription_service.common.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeConverter {
    private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH시 mm분")

    fun convertTimeFormat(datetimeStr: String): String = LocalDateTime.parse(datetimeStr).format(DATETIME_FORMATTER)
    fun convertTimeFormat(datetime: LocalDateTime): String = datetime.format(DATETIME_FORMATTER)
}