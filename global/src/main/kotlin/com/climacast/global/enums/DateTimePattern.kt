package com.climacast.global.enums

enum class DateTimePattern(val pattern: String) {
    ELASTICSEARCH_PATTERN("yyyy/MM/dd HH시 mm분"),
    MYSQL_PATTERN("yyyy-MM-dd'T'HH:mm");

    companion object {
        fun of(datetimePattern: DateTimePattern): String = entries.find { it == datetimePattern }?.pattern
            ?: throw RuntimeException("Cannot find pattern. $datetimePattern")
    }
}