package com.climacast.subscription_service.service

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.annotation.DistributedLock
import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.util.WeatherDataBuffer
import com.climacast.subscription_service.common.util.WeatherDatum
import com.climacast.subscription_service.dto.WeatherQueryDTO
import com.climacast.subscription_service.infra.document.visual.DocumentVisualizerFactory
import com.climacast.subscription_service.infra.subscription.SubscriberInfo
import com.climacast.subscription_service.infra.subscription.SubscriptionHandlerFactory
import com.climacast.subscription_service.model.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.model.repository.HistoryWeatherSearchRepository
import com.climacast.subscription_service.model.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture

@Service
class SubscriptionScheduleService(
    private val subscriptionRepository: SubscriptionRepository,
    private val forecastWeatherSearchRepository: ForecastWeatherSearchRepository,
    private val historyWeatherSearchRepository: HistoryWeatherSearchRepository,
    private val documentVisualizerFactory: DocumentVisualizerFactory,
    private val subscriptionHandlerFactory: SubscriptionHandlerFactory
) {
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional(readOnly = true)
    @DistributedLock(key = "subscribe-30m", waitTime = 0, leaseTime = 60)
    suspend fun sendEveryThirtyMinute() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.THIRTY_MINUTE)

    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    @DistributedLock(key = "subscribe-1h", waitTime = 0, leaseTime = 60)
    suspend fun sendEveryHour() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.ONE_HOUR)

    @Scheduled(cron = "0 0 */3 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    @DistributedLock(key = "subscribe-3h", waitTime = 0, leaseTime = 60)
    suspend fun sendEveryThreeHour() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.THREE_HOURS)

    @Scheduled(cron = "0 0 */6 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    @DistributedLock(key = "subscribe-6h", waitTime = 0, leaseTime = 60)
    suspend fun sendEverySixHour() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.SIX_HOURS)

    @Scheduled(cron = "0 0 0,12 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    @DistributedLock(key = "subscribe-12h", waitTime = 0, leaseTime = 60)
    suspend fun sendEveryTwelveHour() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.TWELVE_HOURS)

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    @DistributedLock(key = "subscribe-1d", waitTime = 0, leaseTime = 60)
    suspend fun sendEveryDay() =
        sendWeatherInformationToSubscribers(SubscriptionInterval.ONE_DAY)

    private suspend fun sendWeatherInformationToSubscribers(interval: SubscriptionInterval) = coroutineScope {
        val subscriptionList = subscriptionRepository.findAllByIntervalsAndStatus(interval)

        subscriptionList.map { subscription ->
            launch {
                val subscriptionMethod = subscription.method
                val subscriptionInfo = subscription.subscriptionInfo

                val subscriptionHandler = subscriptionHandlerFactory.createHandlerByMethod(subscriptionMethod)
                val subscriberInfo = SubscriberInfo(email = subscriptionInfo.email, phoneNumber = subscriptionInfo.phoneNumber)
                subscriptionHandler.setSubscriberInfo(subscriberInfo)

                val weatherType = subscription.weatherType
                val regions = subscription.regions

                val weatherDatumFutures = regions.map { region ->
                    async(Dispatchers.IO) {
                        createWeatherDatumByMethod(subscriptionMethod, weatherType, region)
                    }
                }.awaitAll()

                weatherDatumFutures.forEach { future ->
                    WeatherDataBuffer.store(future.get(), subscriptionMethod)
                }

                regions.forEach { region ->
                    launch(Dispatchers.IO) {
                        WeatherDataBuffer.find(region, subscriptionMethod)?.let {
                            subscriptionHandler.send(it)
                        }
                    }
                }
            }
        }.joinAll()

        WeatherDataBuffer.clear()
    }

    private fun createWeatherDatumByMethod(
        method: SubscriptionMethod,
        weatherType: WeatherType,
        region: String
    ): CompletableFuture<WeatherDatum> {
        val query = WeatherQueryDTO(weatherType, region)
        val weatherDocument = when (query.weatherType) {
            WeatherType.FORECAST -> forecastWeatherSearchRepository.findWeatherByTypeAndRegion(query)
            WeatherType.HISTORY -> historyWeatherSearchRepository.findWeatherByTypeAndRegion(query)
        } ?: throw IllegalArgumentException("Weather data not found for region: ${query.region}")

        val documentVisualizer = documentVisualizerFactory.createDocumentVisualizerByMethod(method)

        return documentVisualizer.convertDocumentAsync(region, weatherDocument, weatherType)
    }
}