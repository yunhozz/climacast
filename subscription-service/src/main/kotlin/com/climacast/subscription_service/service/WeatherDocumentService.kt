package com.climacast.subscription_service.service

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.model.document.ForecastWeather
import com.climacast.subscription_service.model.document.HistoryWeather
import com.climacast.subscription_service.model.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.model.repository.HistoryWeatherSearchRepository
import com.climacast.subscription_service.service.handler.document.AbstractDocumentSaveHandler
import org.springframework.stereotype.Service

@Service
class WeatherDocumentService(
    private val forecastWeatherSearchRepository: ForecastWeatherSearchRepository,
    private val historyWeatherSearchRepository: HistoryWeatherSearchRepository
) : AbstractDocumentSaveHandler() {

    override fun saveForecastWeathers(forecastWeathers: List<ForecastWeather>) {
        forecastWeatherSearchRepository.upsertWeatherDocuments(forecastWeathers, WeatherType.FORECAST)
        log.info("Forecast weathers saved")
    }

    override fun saveHistoryWeathers(historyWeathers: List<HistoryWeather>) {
        historyWeatherSearchRepository.upsertWeatherDocuments(historyWeathers, WeatherType.HISTORY)
        log.info("History weathers saved")
    }
}