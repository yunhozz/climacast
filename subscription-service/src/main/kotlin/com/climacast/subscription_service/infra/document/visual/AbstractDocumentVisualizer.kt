package com.climacast.subscription_service.infra.document.visual

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.model.document.WeatherDocument
import org.openqa.selenium.Dimension
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.net.URI
import java.util.Locale

abstract class AbstractDocumentVisualizer(
    private val templateEngine: TemplateEngine
) : DocumentVisualizer {

    fun createHtml(document: WeatherDocument, type: WeatherType): String =
        templateEngine.process(
            when (type) {
                WeatherType.FORECAST -> DocumentVisualizer.FORECAST_WEATHER_TEMPLATE
                WeatherType.HISTORY -> DocumentVisualizer.HISTORY_WEATHER_TEMPLATE
            }, Context(Locale.getDefault(), mapOf("weatherData" to document))
        )

    companion object {
        fun createWebDriverSession(remoteUrl: String) = RemoteWebDriver(
            URI(remoteUrl).toURL(),
            ChromeOptions().apply {
                addArguments("--no-sandbox", "--headless=new", "--disable-gpu", "--disable-dev-shm-usage")
                addArguments("--font-render-hinting=none", "--lang=ko-KR", "--force-device-scale-factor=0.8")
            }
        )

        fun determineWindowSize(driver: RemoteWebDriver) {
            val pageWidth = driver.executeScript("return document.documentElement.scrollWidth") as Long + 50
            val pageHeight = driver.executeScript("return document.documentElement.scrollHeight") as Long
            driver.manage().window().size = Dimension(pageWidth.toInt(), pageHeight.toInt() / 5)
        }
    }
}