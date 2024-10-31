package com.climacast.batch_server.es.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import java.util.UUID

@Document(indexName = "region")
data class Region(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val parent: String,
    val child: String,
    val nx: String,
    val ny: String
)