package com.climacast.subscription_service.service

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.infra.document.save.AbstractDocumentSaver
import com.climacast.subscription_service.infra.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.infra.repository.HistoryWeatherSearchRepository
import com.climacast.subscription_service.model.document.ForecastWeather
import com.climacast.subscription_service.model.document.HistoryWeather
import com.climacast.subscription_service.model.document.WeatherDocument
import com.climacast.subscription_service.model.dto.WeatherQueryDTO
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class WeatherDocumentService(
    private val forecastWeatherSearchRepository: ForecastWeatherSearchRepository,
    private val historyWeatherSearchRepository: HistoryWeatherSearchRepository
) : AbstractDocumentSaver() {

    override fun saveForecastWeathers(forecastWeathers: List<ForecastWeather>) {
        forecastWeatherSearchRepository.upsertWeatherDocuments(forecastWeathers, WeatherType.FORECAST)
    }

    override fun saveHistoryWeathers(historyWeathers: List<HistoryWeather>) {
        historyWeatherSearchRepository.upsertWeatherDocuments(historyWeathers, WeatherType.HISTORY)
    }

    fun findWeatherListMonoByQuery(query: WeatherQueryDTO): Mono<List<WeatherDocument>> =
        forecastWeatherSearchRepository.findWeatherListMonoByQuery(query)

    fun findWeatherFluxByQuery(query: WeatherQueryDTO): Flux<WeatherDocument> =
        forecastWeatherSearchRepository.findWeatherFluxByQuery(query)
}