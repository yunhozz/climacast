package com.climacast.subscription_service.model.repository.query

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.ElasticsearchException
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch.core.BulkRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.dto.WeatherQueryDTO
import com.climacast.subscription_service.model.document.ForecastWeather
import com.climacast.subscription_service.model.document.HistoryWeather
import com.climacast.subscription_service.model.document.WeatherDocument
import org.springframework.stereotype.Repository

@Repository
class WeatherSearchQueryRepositoryImpl(
    private val elasticsearchClient: ElasticsearchClient
) : WeatherSearchQueryRepository {

    override fun upsertWeatherDocuments(documents: List<WeatherDocument>, type: WeatherType) {
        val bulkRequest = BulkRequest.Builder().apply {
            documents.forEach { document ->
                index(createIndex(type))
                operations { op ->
                    op.update { u ->
                        u.id(document.getId())
                            .action { a ->
                                a.docAsUpsert(true)
                                    .doc(document)
                                    .upsert(mapOf("field" to "value"))
                            }
                    }
                }
            }
        }.build()

        try {
            val bulkResponse = elasticsearchClient.bulk(bulkRequest)
            if (bulkResponse.errors()) {
                throw IllegalArgumentException("Fail to bulk update documents")
            }

        } catch (e: ElasticsearchException) {
            throw IllegalArgumentException("Fail to bulk update documents: ${e.localizedMessage}", e)
        }
    }

    override fun findWeatherByTypeAndRegion(query: WeatherQueryDTO): WeatherDocument? {
        val weatherType = query.weatherType
        val searchRequest = SearchRequest.Builder()
            .index(createIndex(weatherType))
            .query { q ->
                q.match { m ->
                    m.field("region")
                        .query(query.region)
                        .operator(Operator.And)
                }
            }
            .build()

        try {
            val searchResponse = elasticsearchClient.search(searchRequest, determineDocumentClass(weatherType))
            return searchResponse.hits().hits()
                .firstOrNull()
                ?.source()

        } catch (e: ElasticsearchException) {
            throw IllegalArgumentException("Fail to search document: ${e.localizedMessage}", e)
        }
    }

    private fun createIndex(type: WeatherType): String =
        when (type) {
            WeatherType.FORECAST -> "forecast_weather"
            WeatherType.HISTORY -> "history_weather"
        }

    private fun determineDocumentClass(type: WeatherType): Class<out WeatherDocument> =
        when (type) {
            WeatherType.FORECAST -> ForecastWeather::class.java
            WeatherType.HISTORY -> HistoryWeather::class.java
        }
}