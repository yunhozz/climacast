package com.climacast.subscription_service.model.repository.query

import com.climacast.subscription_service.dto.WeatherQueryDTO
import com.climacast.subscription_service.model.document.WeatherDocument

interface WeatherSearchQueryRepository {
    fun findWeatherByRegion(query: WeatherQueryDTO): WeatherDocument?
}