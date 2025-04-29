package com.climacast.subscription_service.infra.document.visual

import com.climacast.global.enums.WeatherType
import com.climacast.global.utils.logger
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.exception.SubscriptionServiceException
import com.climacast.subscription_service.common.util.WeatherDatum
import com.climacast.subscription_service.model.document.WeatherDocument
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebDriverException
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import java.nio.charset.Charset
import java.util.Base64
import java.util.concurrent.CompletableFuture

@Component
class BytesVisualizer(templateEngine: TemplateEngine) : AbstractDocumentVisualizer(templateEngine) {

    private val log = logger()

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
            val bytes = html.toByteArray(Charset.forName("EUC-KR"))
            val encodedHtml = Base64.getEncoder().encodeToString(bytes)

            chromeDriver.get("data:text/html;base64,$encodedHtml")
            determineWindowSize(chromeDriver)

            chromeDriver.getScreenshotAs(OutputType.BYTES)

        } catch (e: Exception) {
            log.error(e.localizedMessage, e)
            when (e) {
                is WebDriverException -> throw SubscriptionServiceException.ChromeWebDriverException()
                else -> throw IllegalArgumentException(e.localizedMessage, e)
            }

        } finally {
            chromeDriver.quit()
        }

        return CompletableFuture.completedFuture(WeatherDatum(region, bytes))
    }

    override fun getSubscriptionMethods(): Array<SubscriptionMethod> = arrayOf(SubscriptionMethod.SLACK)
}