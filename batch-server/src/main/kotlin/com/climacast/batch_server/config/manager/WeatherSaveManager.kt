package com.climacast.batch_server.config.manager

import com.climacast.batch_server.dto.WeatherResponseDTO

interface WeatherSaveManager {
    fun saveOnElasticsearch(weathers: List<WeatherResponseDTO>)
    fun saveOnMysql(weathers: List<WeatherResponseDTO>)
}