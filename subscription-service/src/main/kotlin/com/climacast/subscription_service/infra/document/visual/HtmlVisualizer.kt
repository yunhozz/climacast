package com.climacast.subscription_service.infra.document.visual

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.util.WeatherDatum
import com.climacast.subscription_service.model.document.WeatherDocument
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import java.util.concurrent.CompletableFuture

@Component
class HtmlVisualizer(templateEngine: TemplateEngine) : AbstractDocumentVisualizer(templateEngine) {

    @Async
    override fun convertDocumentAsync(
        region: String,
        document: WeatherDocument,
        type: WeatherType
    ): CompletableFuture<WeatherDatum> {
        val html = createHtml(document, type)
        return CompletableFuture.completedFuture(WeatherDatum(region, html))
    }

    override fun getSubscriptionMethods(): Array<SubscriptionMethod> = arrayOf(SubscriptionMethod.MAIL)
}