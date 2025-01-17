package com.climacast.subscription_service.model.repository.query

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.dto.WeatherQueryDTO
import com.climacast.subscription_service.model.document.WeatherDocument

interface WeatherSearchQueryRepository {
    fun upsertWeatherDocuments(documents: List<WeatherDocument>, type: WeatherType)
    fun findWeatherByTypeAndRegion(query: WeatherQueryDTO): WeatherDocument?
}