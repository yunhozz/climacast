package com.climacast.subscription_service.service.handler.subscription

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
class ImageConverter(
    private val templateEngine: TemplateEngine
) {
    companion object {
        const val WEATHER_TEMPLATE = "weather_template"
        const val WEATHER_IMAGE_LOCAL_DIR = "/Users/yunho/Desktop/project/climacast/subscription-service/src/main/resources/image/weather_image.jpeg"
    }

    fun convertHtmlToImage(data: Any): File {
        val context = Context(Locale.getDefault(), mapOf("weatherData" to data))
        val html = templateEngine.process(WEATHER_TEMPLATE, context)

        try {
            val tempPath = Files.createTempFile("weather_temp", ".html")
            Files.write(tempPath, html.toByteArray())

            return takeScreenShot(tempPath)
                .copyTo(File(WEATHER_IMAGE_LOCAL_DIR), overwrite = true)

        } catch (e: IOException) {
            throw IllegalArgumentException("Fail to write file", e)
        } catch (e: WebDriverException) {
            throw IllegalArgumentException("Fail to upload images to Chrome", e)
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