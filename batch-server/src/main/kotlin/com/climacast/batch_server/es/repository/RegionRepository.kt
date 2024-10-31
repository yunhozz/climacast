package com.climacast.batch_server.es.repository

import com.climacast.batch_server.es.document.Region
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface RegionRepository: ElasticsearchRepository<Region, String>