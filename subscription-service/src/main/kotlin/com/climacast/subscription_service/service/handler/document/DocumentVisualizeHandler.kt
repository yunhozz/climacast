package com.climacast.subscription_service.service.handler.document

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.util.WeatherDatum
import com.climacast.subscription_service.model.document.WeatherDocument
import java.util.concurrent.CompletableFuture

interface DocumentVisualizeHandler {
    fun convertDocumentToHtmlAsync(region: String, document: WeatherDocument, type: WeatherType): CompletableFuture<WeatherDatum>
    fun convertDocumentToImageAsync(region: String, document: WeatherDocument, type: WeatherType): CompletableFuture<WeatherDatum>
}