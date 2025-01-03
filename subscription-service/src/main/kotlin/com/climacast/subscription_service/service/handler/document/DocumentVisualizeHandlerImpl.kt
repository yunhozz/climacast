package com.climacast.subscription_service.service.handler.document

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.model.document.WeatherDocument
import org.openqa.selenium.Dimension
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.Locale

@Component
class DocumentVisualizeHandlerImpl(
    private val templateEngine: TemplateEngine
) : DocumentVisualizeHandler {

    companion object {
        const val FORECAST_WEATHER_TEMPLATE = "forecast_weather_template"
        const val HISTORY_WEATHER_TEMPLATE = "history_weather_template"
        const val WEATHER_IMAGE_LOCAL_DIR = "/Users/yunho/Desktop/project/climacast/subscription-service/src/main/resources/image/"
    }

    override fun convertDocumentToHtml(document: WeatherDocument, type: WeatherType): String {
        val context = Context(Locale.getDefault(), mapOf("weatherData" to document))
        return templateEngine.process(when(type) {
            WeatherType.FORECAST -> FORECAST_WEATHER_TEMPLATE
            WeatherType.HISTORY -> HISTORY_WEATHER_TEMPLATE
        }, context)
    }

    override fun convertDocumentToImage(document: WeatherDocument, type: WeatherType): File {
        val html = convertDocumentToHtml(document, type)
        val chromeDriver = ChromeDriver(ChromeOptions().apply {
            addArguments("--headless", "--disable-gpu")
        })

        try {
            val currentTime = System.currentTimeMillis()
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