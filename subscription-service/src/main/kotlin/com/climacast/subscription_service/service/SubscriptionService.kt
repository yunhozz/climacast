package com.climacast.subscription_service.service

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.util.SubscriptionIntervalConstants
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
        sendWeatherInformationToSubscribers(SubscriptionIntervalConstants.EVERY_THIRTY_MINUTE)

    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEveryHour() =
        sendWeatherInformationToSubscribers(SubscriptionIntervalConstants.EVERY_ONE_HOUR)

    @Scheduled(cron = "0 0 */3 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEveryThreeHour() =
        sendWeatherInformationToSubscribers(SubscriptionIntervalConstants.EVERY_THREE_HOUR)

    @Scheduled(cron = "0 0 */6 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEverySixHour() =
        sendWeatherInformationToSubscribers(SubscriptionIntervalConstants.EVERY_SIX_HOUR)

    @Scheduled(cron = "0 0 0,12 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEveryTwelveHour() =
        sendWeatherInformationToSubscribers(SubscriptionIntervalConstants.EVERY_TWELVE_HOUR)

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    suspend fun sendForecastWeathersEveryDay() =
        sendWeatherInformationToSubscribers(SubscriptionIntervalConstants.EVERY_ONE_DAY)

    private suspend fun sendWeatherInformationToSubscribers(intervals: Set<SubscriptionInterval>) = coroutineScope {
        val subscriptionSummarySet = subscriptionRepository.findSubscriptionSummarySetByIntervals(intervals)
        subscriptionSummarySet.forEach { subscription ->
            val weatherType = subscription.getWeatherType()
            val method = subscription.getMethod()

            subscription.getRegions().map { region ->
                launch {
                    val query = WeatherQueryDTO(weatherType, region)
                    val weather = when (weatherType) {
                        WeatherType.FORECAST -> forecastWeatherSearchRepository.findWeatherByTypeAndRegion(query)
                        WeatherType.HISTORY -> historyWeatherSearchRepository.findWeatherByTypeAndRegion(query)
                    } ?: throw IllegalArgumentException("Weather data not found")

                    val data = if (method == SubscriptionMethod.MAIL) {
                        documentVisualizeHandler.convertDocumentToHtml(weather, weatherType)
                    } else {
                        documentVisualizeHandler.convertDocumentToImage(weather, weatherType)
                    }
                    WeatherDataBuffer.put(region, data)
                }
            }.joinAll()
        }

        subscriptionSummarySet.map { subscription ->
            launch {
                val subscriptionInfo = subscription.getSubscriptionInfo()
                val regions = subscription.getRegions()

                val subscriptionHandler = subscriptionHandlerFactory.createHandlerByMethod(subscription.getMethod())
                val subscriberInfo = SubscriberInfo(email = subscriptionInfo?.email, phoneNumber = subscriptionInfo?.phoneNumber)
                subscriptionHandler.setSubscriberInfo(subscriberInfo)

                regions.forEach { region ->
                    val weatherData = WeatherDataBuffer.find(region)
                    subscriptionHandler.send(weatherData)
                }
            }
        }.joinAll()

        WeatherDataBuffer.clear()
    }
}