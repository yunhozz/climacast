package com.climacast.subscription_service.model.document

interface WeatherDocument : TimeSliceable {
    fun getId(): String
}