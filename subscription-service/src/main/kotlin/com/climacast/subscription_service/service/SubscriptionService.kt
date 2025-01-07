package com.climacast.subscription_service.service

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.util.WeatherDataBuffer
import com.climacast.subscription_service.dto.WeatherQueryDTO
import com.climacast.subscription_service.model.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.model.repository.HistoryWeatherSearchRepository
import com.climacast.subscription_service.model.repository.SubscriptionRepository
import com.climacast.subscription_service.service.handler.document.DocumentVisualizeHandler
import com.climacast.subscription_service.service.handler.subscription.SubscriberInfo
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandlerFactory
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val forecastWeatherSearchRepository: ForecastWeatherSearchRepository,
    private val historyWeatherSearchRepository: HistoryWeatherSearchRepository,
    private val documentVisualizeHandler: DocumentVisualizeHandler,
    private val subscriptionHandlerFactory: SubscriptionHandlerFactory
) {
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEveryThirtyMinute() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.THIRTY_MINUTE)

    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEveryHour() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.ONE_HOUR)

    @Scheduled(cron = "0 0 */3 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEveryThreeHour() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.THREE_HOURS)

    @Scheduled(cron = "0 0 */6 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEverySixHour() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.SIX_HOURS)

    @Scheduled(cron = "0 0 0,12 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEveryTwelveHour() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.TWELVE_HOURS)

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEveryDay() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.ONE_DAY)

    private suspend fun sendWeatherInformationToSubscribers(interval: SubscriptionInterval) = coroutineScope {
        val subscriptionSummarySet = subscriptionRepository.findSubscriptionSummarySetByInterval(interval)
        subscriptionSummarySet.forEach { subscription ->
            val weatherType = subscription.getWeatherType()
            val regions = subscription.getRegions()

            regions.map { region ->
                launch {
                    val query = WeatherQueryDTO(weatherType, region)
                    val weather = when (weatherType) {
                        WeatherType.FORECAST -> forecastWeatherSearchRepository.findWeatherByTypeAndRegion(query)
                        WeatherType.HISTORY -> historyWeatherSearchRepository.findWeatherByTypeAndRegion(query)
                    } ?: throw IllegalArgumentException("Weather data not found")

                    val data = if (subscription.getMethod() == SubscriptionMethod.MAIL) {
                        documentVisualizeHandler.convertDocumentToHtml(weather, weatherType)
                    } else {
                        documentVisualizeHandler.convertDocumentToImage(weather, weatherType)
                    }
                    WeatherDataBuffer.put(region, data)
                }
            }.joinAll()

            val subscriptionInfo = subscription.getSubscriptionInfo()
            val subscriptionHandler = subscriptionHandlerFactory.createHandlerByMethod(subscription.getMethod())
            val subscriberInfo = SubscriberInfo(email = subscriptionInfo?.email, phoneNumber = subscriptionInfo?.phoneNumber)
            subscriptionHandler.setSubscriberInfo(subscriberInfo)

            regions.map { region ->
                launch {
                    val weatherData = WeatherDataBuffer.find(region)
                    subscriptionHandler.send(weatherData)
                }
            }.joinAll()
        }

        WeatherDataBuffer.clear()
    }
}