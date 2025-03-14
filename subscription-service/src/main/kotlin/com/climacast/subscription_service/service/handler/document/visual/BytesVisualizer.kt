package com.climacast.subscription_service.service.handler.document.visual

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.util.WeatherDatum
import com.climacast.subscription_service.model.document.WeatherDocument
import org.openqa.selenium.Dimension
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI
import java.util.Base64
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

@Component
class BytesVisualizer(templateEngine: TemplateEngine) : AbstractDocumentVisualizerHandler(templateEngine) {

    @Value("\${image.weather.url}")
    private lateinit var weatherImageRemoteUrl: String

    @Async
    override fun convertDocumentAsync(
        region: String,
        document: WeatherDocument,
        type: WeatherType
    ): CompletableFuture<WeatherDatum> {
        val html = createHtml(document, type)
        val chromeDriver = createWebDriverSession(weatherImageRemoteUrl)

        val bytes = try {
            val encodedHtml = Base64.getEncoder().encodeToString(html.toByteArray())
            val dataUrl = "data:text/html;base64,$encodedHtml"
            chromeDriver.get(dataUrl)

            determineWindowSize(chromeDriver)

            val baos = ByteArrayOutputStream()
            val screenshot = chromeDriver.getScreenshotAs(OutputType.FILE)
            ImageIO.write(ImageIO.read(screenshot), "jpeg", baos)

            baos.toByteArray()

        } catch (e: Exception) {
            when (e) {
                is IOException -> throw IllegalArgumentException("Fail to write file: ${e.localizedMessage}", e)
                is WebDriverException -> throw IllegalArgumentException("Fail to upload images to Chrome: ${e.localizedMessage}", e)
                else -> throw IllegalArgumentException(e.localizedMessage, e)
            }

        } finally {
            chromeDriver.quit()
        }

        return CompletableFuture.completedFuture(WeatherDatum(region, bytes))
    }

    override fun getSubscriptionMethods(): Array<SubscriptionMethod> = arrayOf(SubscriptionMethod.SLACK)

    private fun createWebDriverSession(url: String) = RemoteWebDriver(
        URI(weatherImageRemoteUrl).toURL(),
        ChromeOptions().apply {
            addArguments("--no-sandbox", "--headless=new", "--disable-gpu")
        }
    )
}