package com.climacast.subscription_service.service.handler.image

import com.climacast.subscription_service.model.document.WeatherDocument
import java.io.File

interface ImageHandler {
    fun convertDocumentToImage(document: WeatherDocument): File
}