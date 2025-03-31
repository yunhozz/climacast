package com.climacast.subscription_service.infra.document.visual

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.util.WeatherDatum
import com.climacast.subscription_service.model.document.WeatherDocument
import java.util.concurrent.CompletableFuture

interface DocumentVisualizer {
    fun convertDocumentAsync(region: String, document: WeatherDocument, type: WeatherType): CompletableFuture<WeatherDatum>
    fun getSubscriptionMethods(): Array<SubscriptionMethod>

    companion object {
        const val FORECAST_WEATHER_TEMPLATE = "forecast_weather_template"
        const val HISTORY_WEATHER_TEMPLATE = "history_weather_template"
    }
}