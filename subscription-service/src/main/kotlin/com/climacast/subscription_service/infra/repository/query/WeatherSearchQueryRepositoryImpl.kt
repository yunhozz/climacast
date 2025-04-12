package com.climacast.subscription_service.infra.repository.query

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.ElasticsearchException
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch.core.BulkRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import com.climacast.global.enums.WeatherType
import com.climacast.global.utils.logger
import com.climacast.subscription_service.common.exception.SubscriptionServiceException
import com.climacast.subscription_service.model.document.ForecastWeather
import com.climacast.subscription_service.model.document.HistoryWeather
import com.climacast.subscription_service.model.document.WeatherDocument
import com.climacast.subscription_service.model.dto.WeatherQueryDTO
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class WeatherSearchQueryRepositoryImpl(
    private val elasticsearchClient: ElasticsearchClient,
    private val reactiveElasticsearchTemplate: ReactiveElasticsearchTemplate
) : WeatherSearchQueryRepository {

    private val log = logger()

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
                throw SubscriptionServiceException.ElasticsearchClientException()
            }

        } catch (e: ElasticsearchException) {
            log.error("Fail to bulk update documents: ${e.localizedMessage}", e)
            throw SubscriptionServiceException.ElasticsearchClientException()
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
            log.error("Fail to search document: ${e.localizedMessage}", e)
            throw SubscriptionServiceException.ElasticsearchClientException()
        }
    }

    override fun findWeatherByQuery(query: WeatherQueryDTO): Mono<WeatherDocument>? {
        val weatherType = query.weatherType
        val criteria = Criteria("region").`is`(query.region.toString())
//            .and(
//                Criteria("time").between(query.startTime, query.endTime))

        return reactiveElasticsearchTemplate.search(
            CriteriaQuery(criteria),
            determineDocumentClass(weatherType),
            IndexCoordinates.of(createIndex(weatherType))
        )
            .map { it.content }
            .next()
            .doOnError { ex ->
                log.error("Fail to search document: ${ex.localizedMessage}", ex)
            }
            .onErrorResume { Mono.empty() }
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