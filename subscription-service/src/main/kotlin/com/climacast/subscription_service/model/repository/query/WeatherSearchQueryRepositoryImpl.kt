package com.climacast.subscription_service.model.repository.query

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
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

    override fun findWeatherByRegion(query: WeatherQueryDTO): WeatherDocument? {
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

        val searchResponse = elasticsearchClient.search(searchRequest, determineDocumentClass(weatherType))

        return searchResponse.hits().hits()
            .firstOrNull()
            ?.source()
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