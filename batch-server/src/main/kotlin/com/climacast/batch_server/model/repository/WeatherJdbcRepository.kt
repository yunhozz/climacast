package com.climacast.batch_server.model.repository

import com.climacast.batch_server.dto.HourlyWeatherUpsertRequestDTO

interface WeatherJdbcRepository {
    fun upsertHourlyWeatherForecasts(hourlyWeathers: Set<HourlyWeatherUpsertRequestDTO>)
}