package com.climacast.subscription_service.service.handler.document

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.model.document.WeatherDocument
import java.io.File

interface DocumentVisualizeHandler {
    fun convertDocumentToHtml(document: WeatherDocument, type: WeatherType): String
    fun convertDocumentToImage(document: WeatherDocument, type: WeatherType): File
}