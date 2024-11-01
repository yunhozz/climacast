package com.climacast.batch_server.model.repository

import com.climacast.batch_server.model.document.Weather
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface WeatherDocumentRepository: ElasticsearchRepository<Weather, String>