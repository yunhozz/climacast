package com.climacast.batch_server.config.manager

import com.climacast.batch_server.dto.WeatherResponseDTO

interface WeatherSaveManager {
    fun saveOnMysql(weathers: List<WeatherResponseDTO>)
    fun saveOnElasticsearch(weathers: List<WeatherResponseDTO>)
}