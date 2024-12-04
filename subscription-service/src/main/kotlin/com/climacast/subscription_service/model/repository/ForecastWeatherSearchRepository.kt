package com.climacast.subscription_service.model.repository

import com.climacast.subscription_service.model.document.ForecastWeather
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface ForecastWeatherSearchRepository: ElasticsearchRepository<ForecastWeather, String>