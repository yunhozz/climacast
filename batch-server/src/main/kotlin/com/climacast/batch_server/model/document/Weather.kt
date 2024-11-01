package com.climacast.batch_server.model.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "weather", storeIdInSource = false)
data class Weather(
    @Id
    val id: String,
    val parentRegion: String,
    val childRegion: String,
    val latitude: Double,
    val longitude: Double,
    val time: String,
    val data: WeatherData
)

data class WeatherData(
    val temperature: Double
)