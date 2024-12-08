package com.climacast.subscription_service.service

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.dto.WeatherQueryDTO
import com.climacast.subscription_service.model.document.ForecastWeather
import com.climacast.subscription_service.model.document.HistoryWeather
import com.climacast.subscription_service.model.document.WeatherDocument
import com.climacast.subscription_service.model.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.model.repository.HistoryWeatherSearchRepository
import com.climacast.subscription_service.service.listener.AbstractWeatherDataProcessor
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class WeatherSearchService(
    private val forecastWeatherSearchRepository: ForecastWeatherSearchRepository,
    private val historyWeatherSearchRepository: HistoryWeatherSearchRepository
): AbstractWeatherDataProcessor() {

    override fun saveForecastWeathers(forecastWeathers: List<ForecastWeather>): Flux<ForecastWeather> =
        forecastWeatherSearchRepository.saveAll(forecastWeathers)
            .doOnError { log.error(it.localizedMessage, it) }
            .onErrorResume { Flux.empty() }

    override fun saveHistoryWeathers(historyWeathers: List<HistoryWeather>): Flux<HistoryWeather> =
        historyWeatherSearchRepository.saveAll(historyWeathers)
            .doOnError { log.error(it.localizedMessage, it) }
            .onErrorResume { Flux.empty() }

    override fun lookupWeathers(query: WeatherQueryDTO): List<WeatherDocument>? {
        return when (query.type) {
            WeatherType.FORECAST -> {
                forecastWeatherSearchRepository.findAll()
                    .collectList()
                    .block()
            }
            WeatherType.HISTORY -> {
                historyWeatherSearchRepository.findAll()
                    .collectList()
                    .block()
            }
        }
    }
}