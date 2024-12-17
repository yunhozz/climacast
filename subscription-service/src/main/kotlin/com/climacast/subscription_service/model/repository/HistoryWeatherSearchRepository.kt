package com.climacast.subscription_service.model.repository

import com.climacast.subscription_service.model.document.HistoryWeather
import com.climacast.subscription_service.model.repository.query.WeatherSearchQueryRepository
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository

interface HistoryWeatherSearchRepository: ReactiveElasticsearchRepository<HistoryWeather, String>, WeatherSearchQueryRepository