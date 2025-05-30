package com.climacast.batch_server.infra.repository

import com.climacast.batch_server.model.dto.HourlyWeatherUpsertRequestDTO

interface WeatherJdbcRepository {
    fun upsertHourlyWeatherForecasts(hourlyWeathers: Set<HourlyWeatherUpsertRequestDTO>)
}