package com.climacast.subscription_service.service.handler.subscription

import com.climacast.subscription_service.model.document.ForecastWeather
import com.slack.api.Slack
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.webhook.Payload
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class SlackHandler : SubscriptionHandler {

    @Value("\${slack.webhook.url}")
    private lateinit var webhookUrl: String

    override fun setSubscriberInfo(info: SubscriberInfo) {}

    @Async
    override fun send(data: Any) {
        val slack = Slack.getInstance()
        val payload = Payload.builder()
            .blocks(listOf(
                SectionBlock.builder()
                    .text(MarkdownTextObject(createMarkdownTemplate(data), false))
                    .build()
            ))
            .build()

        val response = slack.send(webhookUrl, payload)
        if (response.code != 200) {
            throw IllegalArgumentException("Fail to send data on Slack: code=${response.code}, message=${response.message}")
        }
    }

    override fun getHandlerName() = SubscriptionHandlerName.SLACK

    companion object {
        fun createMarkdownTemplate(data: Any): String {
            val forecastWeather = data as ForecastWeather
            return StringBuilder().apply {
                append("## Weather Information\n\n")
                append("**Region:** ${forecastWeather.region}\n\n")
                append("| Time | Weather Status | Temperature (2m) | Temperature (80m) | Temperature (120m) | Temperature (180m) | Wind Speed (10m) | Wind Speed (80m) | Wind Speed (120m) | Wind Speed (180m) | Humidity |\n")
                append("|------|----------------|------------------|-------------------|--------------------|--------------------|------------------|------------------|-------------------|-------------------|----------|\n")

                forecastWeather.time?.forEachIndexed { index, time ->
                    append("| $time ")
                    append("| ${forecastWeather.weatherStatus?.get(index)} ")
                    append("| ${forecastWeather.temperature2m?.get(index)} ")
                    append("| ${forecastWeather.temperature80m?.get(index)} ")
                    append("| ${forecastWeather.temperature120m?.get(index)} ")
                    append("| ${forecastWeather.temperature180m?.get(index)} ")
                    append("| ${forecastWeather.windSpeed10m?.get(index)} ")
                    append("| ${forecastWeather.windSpeed80m?.get(index)} ")
                    append("| ${forecastWeather.windSpeed120m?.get(index)} ")
                    append("| ${forecastWeather.windSpeed180m?.get(index)} ")
                    append("| ${forecastWeather.humidity2m?.get(index)} |\n")
                }
            }.toString()
        }
    }
}