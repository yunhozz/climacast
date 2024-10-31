package com.climacast.batch_server.es.repository

import com.climacast.batch_server.es.document.ShortTermWeather
import com.climacast.batch_server.es.document.UltraShortTermWeather
import com.climacast.batch_server.es.document.Weather
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface WeatherRepository<T: Weather>: ElasticsearchRepository<T, String> {
    fun findByRegionId(regionId: String): List<T>
}

interface ShortTermWeatherRepository: WeatherRepository<ShortTermWeather>

interface UltraShortTermWeatherRepository: WeatherRepository<UltraShortTermWeather>