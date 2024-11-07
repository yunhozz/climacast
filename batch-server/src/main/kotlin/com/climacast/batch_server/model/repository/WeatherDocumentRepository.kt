package com.climacast.batch_server.model.repository

import com.climacast.batch_server.model.document.WeatherDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface WeatherDocumentRepository: ElasticsearchRepository<WeatherDocument, String>