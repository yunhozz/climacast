package com.climacast.subscription_service.service.handler.document.visual

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.util.WeatherDatum
import com.climacast.subscription_service.model.document.WeatherDocument
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebDriverException
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import java.io.File
import java.io.IOException
import java.util.concurrent.CompletableFuture

@Component
class ImageVisualizer(templateEngine: TemplateEngine) : AbstractDocumentVisualizer(templateEngine) {

    @Value("\${image.weather.url}")
    private lateinit var weatherImageRemoteUrl: String

    @Value("\${image.weather.dir}")
    private lateinit var weatherImageDir: String

    @Async
    override fun convertDocumentAsync(
        region: String,
        document: WeatherDocument,
        type: WeatherType
    ): CompletableFuture<WeatherDatum> {
        val html = createHtml(document, type)
        val chromeDriver = createWebDriverSession(weatherImageRemoteUrl)

        val image = try {
            val currentTime = System.nanoTime()
            val tempFile = File("$weatherImageDir/weather_temp_$currentTime.html")
            tempFile.writeText(html)
            tempFile.deleteOnExit()
            chromeDriver.get("file:///${tempFile.absolutePath}")

            determineWindowSize(chromeDriver)

            val outputFile = File("$weatherImageDir/weather_image_$currentTime.jpeg")
            outputFile.deleteOnExit()

            chromeDriver.getScreenshotAs(OutputType.FILE).copyTo(outputFile)

        } catch (e: Exception) {
            when (e) {
                is IOException -> throw IllegalArgumentException("Fail to write file: ${e.localizedMessage}", e)
                is WebDriverException -> throw IllegalArgumentException("Fail to upload images to Chrome: ${e.localizedMessage}", e)
                else -> throw IllegalArgumentException(e.localizedMessage, e)
            }

        } finally {
            chromeDriver.quit()
        }

        return CompletableFuture.completedFuture(WeatherDatum(region, image))
    }

    override fun getSubscriptionMethods(): Array<SubscriptionMethod> = arrayOf(SubscriptionMethod.SMS)
}