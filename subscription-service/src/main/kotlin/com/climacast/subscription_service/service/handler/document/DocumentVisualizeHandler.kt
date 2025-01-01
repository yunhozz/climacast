package com.climacast.subscription_service.service.handler.document

import com.climacast.subscription_service.model.document.WeatherDocument
import java.io.File

interface DocumentVisualizeHandler {
    fun convertDocumentToHtml(document: WeatherDocument): String
    fun convertDocumentToImage(document: WeatherDocument): File
}