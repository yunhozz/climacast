package com.climacast.subscription_service.service

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.dto.WeatherQueryDTO
import com.climacast.subscription_service.model.entity.Subscription
import com.climacast.subscription_service.model.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.model.repository.HistoryWeatherSearchRepository
import com.climacast.subscription_service.model.repository.SubscriptionRepository
import com.climacast.subscription_service.service.handler.image.ImageHandler
import com.climacast.subscription_service.service.handler.subscription.SubscriberInfo
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandlerFactory
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandlerName
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val forecastWeatherSearchRepository: ForecastWeatherSearchRepository,
    private val historyWeatherSearchRepository: HistoryWeatherSearchRepository,
    private val imageHandler: ImageHandler,
    private val subscriptionHandlerFactory: SubscriptionHandlerFactory
) {
    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEveryHour() = coroutineScope {
        val subscriptionList = subscriptionRepository.findAllByIntervalsAndStatus(SubscriptionInterval.ONE_HOUR)
        val subscriptionIds = subscriptionList.map { it.id!! }

        val regions = subscriptionRepository.findRegionsByIds(subscriptionIds)
        val weatherImages = fetchWeatherImagesByRegions(regions)

        subscriptionList.map { subscription ->
            async {
                sendWeatherImagesToSubscriber(subscription, weatherImages)
            }.await()
        }
    }

    private suspend fun fetchWeatherImagesByRegions(regions: Set<String>): Map<String, File> = coroutineScope {
        val weatherImages = mutableMapOf<String, File>()
        regions.map { region ->
            async {
                val query = WeatherQueryDTO(WeatherType.FORECAST, region)
                val forecastWeather = forecastWeatherSearchRepository.findWeatherByRegion(query)
                    ?: throw IllegalArgumentException("Weather data not found")

                val weatherImage = imageHandler.convertDocumentToImage(forecastWeather)
                weatherImages[region] = weatherImage
            }
        }.awaitAll()
        weatherImages
    }

    private suspend fun sendWeatherImagesToSubscriber(subscription: Subscription, weatherImages: Map<String, File>) {
        val subscriptionInfo = subscription.subscriptionInfo
        val regions = subscription.regions

        when (subscription.method) {
            SubscriptionMethod.MAIL -> {
                val mailHandler = subscriptionHandlerFactory.findHandler(SubscriptionHandlerName.MAIL)
                mailHandler.setSubscriberInfo(SubscriberInfo(email = subscriptionInfo.email))
                regions.forEach { mailHandler.send(weatherImages[it]!!) }
            }
            SubscriptionMethod.SLACK -> {
                val slackHandler = subscriptionHandlerFactory.findHandler(SubscriptionHandlerName.SLACK)
                regions.forEach { slackHandler.send(weatherImages[it]!!) }
            }
            SubscriptionMethod.SMS -> {
                val smsHandler = subscriptionHandlerFactory.findHandler(SubscriptionHandlerName.SMS)
                smsHandler.setSubscriberInfo(SubscriberInfo(phoneNumber = subscriptionInfo.phoneNumber))
                regions.forEach { smsHandler.send(weatherImages[it]!!) }
            }
        }
    }
}