package com.climacast.subscription_service.service

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.util.WeatherDataBuffer
import com.climacast.subscription_service.common.util.WeatherDatum
import com.climacast.subscription_service.model.document.WeatherDocument
import com.climacast.subscription_service.model.entity.Subscription
import com.climacast.subscription_service.model.entity.SubscriptionInfo
import com.climacast.subscription_service.model.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.model.repository.HistoryWeatherSearchRepository
import com.climacast.subscription_service.model.repository.SubscriptionRepository
import com.climacast.subscription_service.service.handler.document.DocumentVisualizeHandler
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandler
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandlerFactory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.anyBoolean
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.doNothing
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.times
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis

@ExtendWith(MockitoExtension::class)
class SubscriptionServiceTests {
    @InjectMocks
    private lateinit var subscriptionService: SubscriptionService
    @Mock
    private lateinit var subscriptionRepository: SubscriptionRepository
    @Mock
    private lateinit var forecastWeatherSearchRepository: ForecastWeatherSearchRepository
    @Mock
    private lateinit var historyWeatherSearchRepository: HistoryWeatherSearchRepository
    @Mock
    private lateinit var documentVisualizeHandler: DocumentVisualizeHandler
    @Mock
    private lateinit var subscriptionHandlerFactory: SubscriptionHandlerFactory

    private val subscription = Subscription(
        subscriptionInfo = SubscriptionInfo(email = "test@example.com", phoneNumber = "1234567890"),
        regions = setOf("region1", "region2", "region3"),
        weatherType = WeatherType.FORECAST,
        intervals = SubscriptionInterval.THIRTY_MINUTE,
        method = SubscriptionMethod.MAIL
    )

    @BeforeEach
    fun beforeEach() {
        WeatherDataBuffer.init()
        repeat(3) {
            WeatherDataBuffer.store(
                WeatherDatum("region${it + 1}", "resource"),
                SubscriptionMethod.MAIL
            )
        }
    }

    @AfterEach
    fun afterEach() {
        WeatherDataBuffer.clear()
    }

    @Test
    fun sendWeatherInformationToSubscribers() = runBlocking<Unit> {
        // given
        val weatherDocument = mock(WeatherDocument::class.java)
        val subscriptionHandler = mock(SubscriptionHandler::class.java)
        val weatherDatum = WeatherDatum("region", "resource")

        given(subscriptionRepository.findAllByIntervalsAndStatus(any(), anyBoolean()))
            .willReturn(listOf(subscription))
        given(subscriptionHandlerFactory.createHandlerByMethod(any()))
            .willReturn(subscriptionHandler)
        doNothing()
            .`when`(subscriptionHandler).setSubscriberInfo(any())
        given(forecastWeatherSearchRepository.findWeatherByTypeAndRegion(any()))
            .willReturn(weatherDocument)
        given(documentVisualizeHandler.convertDocumentToHtmlAsync(anyString(), any(), any()))
            .willReturn(CompletableFuture.completedFuture(weatherDatum))
        doNothing()
            .`when`(subscriptionHandler).send(any())

        // when
        subscriptionService.sendForecastWeathersEveryThirtyMinute()

        // then
        val regionSize = subscription.regions.size
        then(forecastWeatherSearchRepository).should(times(regionSize))
            .findWeatherByTypeAndRegion(any())
        then(subscriptionHandler).should(times(regionSize))
            .send(any())
    }

    @Test
    fun handleEmptySubscriptionList() = runBlocking {
        // given
        given(subscriptionRepository.findAllByIntervalsAndStatus(any(), anyBoolean()))
            .willReturn(emptyList())

        // when
        subscriptionService.sendForecastWeathersEveryThirtyMinute()

        // then
        then(subscriptionHandlerFactory).shouldHaveNoInteractions()
        then(forecastWeatherSearchRepository).shouldHaveNoInteractions()
        then(documentVisualizeHandler).shouldHaveNoInteractions()
    }

    @Test
    fun handleWeatherDataNotFound() = runBlocking {
        // given
        val subscriptionHandler = mock(SubscriptionHandler::class.java)

        given(subscriptionRepository.findAllByIntervalsAndStatus(any(), anyBoolean()))
            .willReturn(listOf(subscription))
        given(subscriptionHandlerFactory.createHandlerByMethod(any()))
            .willReturn(subscriptionHandler)
        doNothing()
            .`when`(subscriptionHandler).setSubscriberInfo(any())
        given(forecastWeatherSearchRepository.findWeatherByTypeAndRegion(any()))
            .willReturn(null)

        try {
            // when
            subscriptionService.sendForecastWeathersEveryThirtyMinute()
        } catch (e: IllegalArgumentException) {
            // then
            assert(e.message == "Weather data not found for region: region1")
            then(documentVisualizeHandler).shouldHaveNoInteractions()
        }
    }

    @Test
    fun measureTimeForHandleMultipleSubscriptions() = runBlocking {
        // given
        val weatherDocument = mock(WeatherDocument::class.java)
        val subscriptionHandler = mock(SubscriptionHandler::class.java)
        val weatherDatum = WeatherDatum("region", "resource")

        val numOfSubscription = 100000
        val subscriptionList = (1..numOfSubscription).map { subscription }

        given(subscriptionRepository.findAllByIntervalsAndStatus(any(), anyBoolean()))
            .willReturn(subscriptionList)
        given(subscriptionHandlerFactory.createHandlerByMethod(any()))
            .willReturn(subscriptionHandler)
        doNothing()
            .`when`(subscriptionHandler).setSubscriberInfo(any())
        given(forecastWeatherSearchRepository.findWeatherByTypeAndRegion(any()))
            .willReturn(weatherDocument)
        given(documentVisualizeHandler.convertDocumentToHtmlAsync(anyString(), any(), any()))
            .willReturn(CompletableFuture.completedFuture(weatherDatum))
        doNothing()
            .`when`(subscriptionHandler).send(any())

        // when
        val time = measureTimeMillis {
            subscriptionService.sendForecastWeathersEveryThirtyMinute()
        }

        // then
        println("Total execution time: $time ms")

        /*
        Measure the time taken based on the number of subscriptions (Assume that it takes 2 seconds per iteration)
        1 -> 2015 ms
        10 -> 2021 ms
        10^2 -> 2046 ms
        10^3 -> 2139 ms
        10^4 -> 2645 ms
        10^5 -> 5449 ms
        10^6 -> java.lang.OutOfMemoryError: Java heap space
         */
    }
}