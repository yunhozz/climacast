package com.climacast.subscription_service.model.repository

import com.climacast.subscription_service.model.document.ForecastWeather
import com.climacast.subscription_service.model.repository.query.WeatherSearchQueryRepository
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository

interface ForecastWeatherSearchRepository: ReactiveElasticsearchRepository<ForecastWeather, String>, WeatherSearchQueryRepository