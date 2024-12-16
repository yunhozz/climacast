package com.climacast.subscription_service.service

import com.climacast.subscription_service.model.document.ForecastWeather
import com.climacast.subscription_service.model.document.HistoryWeather
import com.climacast.subscription_service.model.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.model.repository.HistoryWeatherSearchRepository
import com.climacast.subscription_service.service.handler.data.AbstractWeatherDataHandler
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class WeatherDataService(
    private val forecastWeatherSearchRepository: ForecastWeatherSearchRepository,
    private val historyWeatherSearchRepository: HistoryWeatherSearchRepository
) : AbstractWeatherDataHandler() {

    // TODO: 날씨 예보 데이터 입력 시 중복 제거

    override fun saveForecastWeathers(forecastWeathers: List<ForecastWeather>): Flux<ForecastWeather> =
        forecastWeatherSearchRepository.saveAll(forecastWeathers)
            .doOnComplete { log.info("Forecast Weathers Saved") }
            .doOnError { log.error(it.localizedMessage, it) }
            .onErrorResume { Flux.empty() }

    override fun saveHistoryWeathers(historyWeathers: List<HistoryWeather>): Flux<HistoryWeather> =
        historyWeatherSearchRepository.saveAll(historyWeathers)
            .doOnComplete { log.info("History Weathers Saved") }
            .doOnError { log.error(it.localizedMessage, it) }
            .onErrorResume { Flux.empty() }
}