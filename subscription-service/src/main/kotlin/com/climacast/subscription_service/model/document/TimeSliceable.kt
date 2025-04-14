package com.climacast.subscription_service.model.document

interface TimeSliceable {
    fun sliceByTime(startTime: String, endTime: String): WeatherDocument

    fun <T> List<T>.slice(timeList: List<String>?, startTime: String, endTime: String): List<T> {
        val startIdx = timeList?.indexOfFirst { it == startTime }
        val endIdx = timeList?.indexOfLast { it == endTime }
        return this.subList(startIdx!!, endIdx!! + 1)
    }
}