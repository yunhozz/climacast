package com.climacast.subscription_service.model.document

import com.climacast.global.utils.DateTimeConverter

interface WeatherDocument {
    fun getId(): String
    fun getTimeList(): List<String>

    fun sliceByTime(startTime: String, endTime: String): WeatherDocument

    fun sliceByDay(startTime: String? = null, endTime: String? = null): List<WeatherDocument> {
        val timeList = this.getTimeList().let { tl ->
            if (!startTime.isNullOrBlank() && !endTime.isNullOrBlank()) {
                tl.filter { it in startTime .. endTime }
            } else tl
        }

        return timeList.mapIndexed { index, time ->
            val localDateTime = DateTimeConverter.convertToLocalDateTime(time)
            localDateTime.toLocalDate() to index
        }
            .groupBy({ it.first }, { it.second })
            .map { (_, indices) ->
                val st = timeList[indices.first()]
                val et = timeList[indices.last()]
                sliceByTime(st, et)
            }
    }

    fun <T> List<T>.slice(timeList: List<String>?, startTime: String, endTime: String): List<T> {
        val startIdx = timeList?.indexOfFirst { it == startTime }
        val endIdx = timeList?.indexOfLast { it == endTime }
        return this.subList(startIdx!!, endIdx!! + 1)
    }
}