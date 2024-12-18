package com.climacast.subscription_service.service

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.dto.WeatherQueryDTO
import com.climacast.subscription_service.model.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.model.repository.HistoryWeatherSearchRepository
import com.climacast.subscription_service.service.handler.subscription.SubscriberInfo
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandlerFactory
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandlerName
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val forecastWeatherSearchRepository: ForecastWeatherSearchRepository,
    private val historyWeatherSearchRepository: HistoryWeatherSearchRepository,
    private val subscriptionHandlerFactory: SubscriptionHandlerFactory
) {
    // TODO: 구독자 및 지역에 대한 관계 테이블 생성
    private val subscriberMap = LinkedHashMap<String, List<String>>().apply {
        this["qkrdbsgh1121@naver.com"] = listOf(
            "서울특별시 서대문구",
            "서울특별시 강남구",
            "서울특별시 종로구"
        )
    }

    @Scheduled(cron = "0 0 * * * *")
    suspend fun sendForecastWeathersOnEmailEveryHour() = produceForecastWeathersOnEmail()

    @Scheduled(cron = "0 0 * * * *")
    suspend fun sendForecastWeathersOnSlackEveryHour() = produceForecastWeathersOnSlack()

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    suspend fun sendForecastWeathersEmailEveryDay() = produceForecastWeathersOnEmail()

    private suspend fun produceForecastWeathersOnEmail() = coroutineScope {
        val mailHandler = subscriptionHandlerFactory.findHandler(SubscriptionHandlerName.MAIL)
        subscriberMap.map { (email, regions) ->
            async {
                regions.forEach { region ->
                    val query = WeatherQueryDTO(WeatherType.FORECAST, region)
                    val forecastWeather = forecastWeatherSearchRepository.findWeatherByRegion(query)
                        ?: throw IllegalArgumentException("Weather data not found")

                    mailHandler.setSubscriberInfo(SubscriberInfo(email))
                    mailHandler.send(forecastWeather)
                }
            }
        }.awaitAll()
    }

    private suspend fun produceForecastWeathersOnSlack() = coroutineScope {
        val slackHandler = subscriptionHandlerFactory.findHandler(SubscriptionHandlerName.SLACK)
        subscriberMap.map { (_, regions) ->
            async {
                regions.forEach { region ->
                    val query = WeatherQueryDTO(WeatherType.FORECAST, region)
                    val forecastWeather = forecastWeatherSearchRepository.findWeatherByRegion(query)
                        ?: throw IllegalArgumentException("Weather data not found")

                    slackHandler.send(forecastWeather)
                }
            }
        }.awaitAll()
    }
}