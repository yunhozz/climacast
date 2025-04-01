package com.climacast.subscription_service.service

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.util.WeatherDataBuffer
import com.climacast.subscription_service.common.util.WeatherDatum
import com.climacast.subscription_service.infra.document.visual.DocumentVisualizer
import com.climacast.subscription_service.infra.document.visual.DocumentVisualizerFactory
import com.climacast.subscription_service.infra.subscription.SubscriptionHandler
import com.climacast.subscription_service.infra.subscription.SubscriptionHandlerFactory
import com.climacast.subscription_service.model.document.WeatherDocument
import com.climacast.subscription_service.model.entity.Subscription
import com.climacast.subscription_service.model.entity.SubscriptionInfo
import com.climacast.subscription_service.model.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.model.repository.HistoryWeatherSearchRepository
import com.climacast.subscription_service.model.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
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
class SubscriptionServiceTest {
    @InjectMocks
    private lateinit var subscriptionService: SubscriptionService
    @Mock
    private lateinit var subscriptionRepository: SubscriptionRepository
    @Mock
    private lateinit var forecastWeatherSearchRepository: ForecastWeatherSearchRepository
    @Mock
    private lateinit var historyWeatherSearchRepository: HistoryWeatherSearchRepository
    @Mock
    private lateinit var documentVisualizerFactory: DocumentVisualizerFactory
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
        val documentVisualizer = mock(DocumentVisualizer::class.java)
        val weatherDatum = WeatherDatum("region", "resource")

        given(subscriptionRepository.findAllByIntervalsAndStatus(any(), anyBoolean()))
            .willReturn(listOf(subscription))
        given(subscriptionHandlerFactory.createHandlerByMethod(any()))
            .willReturn(subscriptionHandler)
        doNothing()
            .`when`(subscriptionHandler).setSubscriberInfo(any())
        given(forecastWeatherSearchRepository.findWeatherByTypeAndRegion(any()))
            .willReturn(weatherDocument)
        given(documentVisualizerFactory.createDocumentVisualizerByMethod(any()))
            .willReturn(documentVisualizer)
        given(documentVisualizer.convertDocumentAsync(anyString(), any(), any()))
            .willReturn(CompletableFuture.completedFuture(weatherDatum))
        doNothing()
            .`when`(subscriptionHandler).send(any())

        // when
        subscriptionService.sendEveryThirtyMinute()

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
        subscriptionService.sendEveryThirtyMinute()

        // then
        then(subscriptionHandlerFactory).shouldHaveNoInteractions()
        then(forecastWeatherSearchRepository).shouldHaveNoInteractions()
        then(documentVisualizerFactory).shouldHaveNoInteractions()
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
            subscriptionService.sendEveryThirtyMinute()
        } catch (e: IllegalArgumentException) {
            // then
            assert(e.message!!.contains("Weather data not found for region"))
            then(documentVisualizerFactory).shouldHaveNoInteractions()
        }
    }

    @Test
    fun measureTimeForSendingWeatherInformationToSubscribers() = runBlocking {
        // given
        val nSubscriptionList = 10
        val nRegions = 5

        val jobs = (1..nSubscriptionList).map {
            launch {
                println("Set Subscription Handler on ${Thread.currentThread().name}")
                delay(100)

                (1..nRegions).map {
                    async(Dispatchers.IO) {
                        println("Convert Document to Resource on ${Thread.currentThread().name}")
                        delay(1000)
                    }
                }.awaitAll()

                delay(100)

                repeat(nRegions) {
                    launch(Dispatchers.IO) {
                        println("Send Weather Data to Subscriber on ${Thread.currentThread().name}")
                        delay(1000)
                    }
                }
            }
        }

        // when
        val time = measureTimeMillis {
            jobs.joinAll()
        }

        // then
        println("Total execution time: $time ms")

        /*
        Number of subscriptions : Time

        1    : 2224 ms
        10   : 2227 ms
        10^2 : 2243 ms
        10^3 : 2280 ms
        10^4 : 2643 ms
        10^5 : 5022 ms
         */
    }
}