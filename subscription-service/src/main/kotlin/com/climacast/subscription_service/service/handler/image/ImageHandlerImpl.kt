package com.climacast.subscription_service.service.handler.image

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
import java.nio.file.Path
import java.util.Locale

@Component
class ImageHandlerImpl(
    private val templateEngine: TemplateEngine
) : ImageHandler {

    companion object {
        const val WEATHER_TEMPLATE = "weather_template"
        const val WEATHER_IMAGE_LOCAL_DIR = "/Users/yunho/Desktop/project/climacast/subscription-service/src/main/resources/image/"
    }

    override fun convertDocumentToImage(document: WeatherDocument): File {
        val context = Context(Locale.getDefault(), mapOf("weatherData" to document))
        val html = templateEngine.process(WEATHER_TEMPLATE, context)

        try {
            val currentTime = System.currentTimeMillis()
            val filePath = Files.createTempFile("weather_temp_$currentTime", ".html")
            Files.write(filePath, html.toByteArray())

            return takeScreenShot(filePath)
                .copyTo(File("$WEATHER_IMAGE_LOCAL_DIR$currentTime.jpeg"))

        } catch (e: IOException) {
            throw IllegalArgumentException("Fail to write file: ${e.localizedMessage}", e)
        } catch (e: WebDriverException) {
            throw IllegalArgumentException("Fail to upload images to Chrome: ${e.localizedMessage}", e)
        }
    }

    private fun takeScreenShot(path: Path): File {
        val chromeDriver = ChromeDriver(ChromeOptions().apply {
            addArguments("--headless", "--disable-gpu")
        })

        try {
            chromeDriver.get(path.toUri().toString())

            val pageWidth = chromeDriver.executeScript("return document.documentElement.scrollWidth") as Long + 50
            val pageHeight = chromeDriver.executeScript("return document.documentElement.scrollHeight") as Long + 50
            chromeDriver.manage().window().size = Dimension(pageWidth.toInt(), pageHeight.toInt())

            return chromeDriver.getScreenshotAs(OutputType.FILE)

        } finally {
            chromeDriver.quit()
        }
    }
}