package com.climacast.subscription_service.service.handler.document

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.util.WeatherDatum
import com.climacast.subscription_service.model.document.WeatherDocument
import org.openqa.selenium.Dimension
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.util.Locale
import java.util.concurrent.CompletableFuture

@Component
class DocumentVisualizeHandlerImpl(
    private val templateEngine: TemplateEngine
) : DocumentVisualizeHandler {

    companion object {
        const val FORECAST_WEATHER_TEMPLATE = "forecast_weather_template"
        const val HISTORY_WEATHER_TEMPLATE = "history_weather_template"
        const val WEATHER_IMAGE_LOCAL_DIR = "/Users/yunho/Desktop/project/climacast/subscription-service/src/main/resources/image/"
        const val SELENIUM_CHROME_REMOTE_URL = "http://selenium-chrome:4444"
    }

    @Async
    override fun convertDocumentToHtmlAsync(
        region: String,
        document: WeatherDocument,
        type: WeatherType
    ): CompletableFuture<WeatherDatum> = CompletableFuture.completedFuture(
        WeatherDatum(region, convertDocumentToHtml(document, type))
    )

    @Async
    override fun convertDocumentToImageAsync(
        region: String,
        document: WeatherDocument,
        type: WeatherType
    ): CompletableFuture<WeatherDatum> = CompletableFuture.completedFuture(
        WeatherDatum(region, convertDocumentToImage(document, type))
    )

    private fun convertDocumentToHtml(document: WeatherDocument, type: WeatherType): String =
        templateEngine.process(
            when(type) {
                WeatherType.FORECAST -> FORECAST_WEATHER_TEMPLATE
                WeatherType.HISTORY -> HISTORY_WEATHER_TEMPLATE
            }, Context(Locale.getDefault(), mapOf("weatherData" to document))
        )

    private fun convertDocumentToImage(document: WeatherDocument, type: WeatherType): File {
        val html = convertDocumentToHtml(document, type)
        val chromeDriver = RemoteWebDriver(
            URI(SELENIUM_CHROME_REMOTE_URL).toURL(),
            ChromeOptions().apply {
                addArguments("--no-sandbox", "--headless", "--disable-gpu")
            }
        )

        try {
            val currentTime = System.nanoTime()
            val filePath = Files.createTempFile("weather_temp_$currentTime", ".html")
            Files.write(filePath, html.toByteArray())

            chromeDriver.get(filePath.toUri().toString())
            val pageWidth = chromeDriver.executeScript("return document.documentElement.scrollWidth") as Long + 50
            val pageHeight = chromeDriver.executeScript("return document.documentElement.scrollHeight") as Long + 50
            chromeDriver.manage().window().size = Dimension(pageWidth.toInt(), pageHeight.toInt())

            return chromeDriver.getScreenshotAs(OutputType.FILE)
                .copyTo(File("$WEATHER_IMAGE_LOCAL_DIR$currentTime.jpeg"))

        } catch (e: Exception) {
            when (e) {
                is IOException -> throw IllegalArgumentException("Fail to write file: ${e.localizedMessage}", e)
                is WebDriverException -> throw IllegalArgumentException("Fail to upload images to Chrome: ${e.localizedMessage}", e)
                else -> throw IllegalArgumentException(e.localizedMessage, e)
            }

        } finally {
            chromeDriver.quit()
        }
    }
}