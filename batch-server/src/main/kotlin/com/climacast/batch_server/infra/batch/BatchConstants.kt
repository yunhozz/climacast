package com.climacast.batch_server.infra.batch

object BatchConstants {
    const val CSV_PATH = "/static/region-list.csv"
    const val WEATHER_FORECAST_JOB = "weatherForecast"
    const val WEATHER_HISTORY_JOB = "weatherHistory"
    const val CALL_HISTORICAL_WEATHER_OPEN_API_STEP = "callHistoricalWeatherOpenApi"
    const val CALL_FORECAST_WEATHER_OPEN_API_STEP = "callForecastWeatherOpenApi"
    const val SAVE_WEATHER_DATA_STEP = "saveWeatherData"
    const val WEATHER_API_CHUNK_SIZE = 5
}