package com.climacast.subscription_service.service.handler.document.visual

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.model.document.WeatherDocument
import org.openqa.selenium.Dimension
import org.openqa.selenium.remote.RemoteWebDriver
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.Locale

abstract class AbstractDocumentVisualizerHandler(
    private val templateEngine: TemplateEngine
) : DocumentVisualizeHandler {

    override fun createHtml(document: WeatherDocument, type: WeatherType): String =
        templateEngine.process(
            when (type) {
                WeatherType.FORECAST -> DocumentVisualizeHandler.FORECAST_WEATHER_TEMPLATE
                WeatherType.HISTORY -> DocumentVisualizeHandler.HISTORY_WEATHER_TEMPLATE
            }, Context(Locale.getDefault(), mapOf("weatherData" to document))
        )

    fun determineWindowSize(driver: RemoteWebDriver) {
        val pageWidth = driver.executeScript("return document.documentElement.scrollWidth") as Long + 50
        val pageHeight = driver.executeScript("return document.documentElement.scrollHeight") as Long + 50
        driver.manage().window().size = Dimension(192, 192)
    }
}