package com.climacast.batch_server.infra.data

import com.climacast.batch_server.model.dto.HourlyWeatherUpsertRequestDTO
import com.climacast.batch_server.model.entity.DailyWeather
import com.climacast.batch_server.model.entity.HourlyWeather

sealed class ConvertedWeatherData {
    data class WeatherForecastData(
        val hourlyWeatherUpsertDTOs: Set<HourlyWeatherUpsertRequestDTO>
    ): ConvertedWeatherData()

    data class WeatherHistoryData(
        val dailyWeathers: Set<DailyWeather>,
        val hourlyWeathers: Set<HourlyWeather>
    ): ConvertedWeatherData()
}