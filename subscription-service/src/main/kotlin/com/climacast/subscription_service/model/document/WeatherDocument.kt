package com.climacast.subscription_service.model.document

import com.climacast.subscription_service.common.util.DateTimeConverter

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

        val timeIndexListMap = timeList.mapIndexed { index, time ->
            val localDateTime = DateTimeConverter.parseDateTime(time)
            localDateTime.toLocalDate() to index
        }
        .groupBy({ it.first }, { it.second })

        return timeIndexListMap.map { (_, indices) ->
            sliceByTime(timeList[indices.first()], timeList[indices.last()])
        }
    }

    fun <T> List<T>.slice(timeList: List<String>?, startTime: String, endTime: String): List<T> {
        val startIdx = timeList?.indexOfFirst { it == startTime }
        val endIdx = timeList?.indexOfLast { it == endTime }
        return this.subList(startIdx!!, endIdx!! + 1)
    }
}