package com.climacast.batch_server.es.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.util.UUID

abstract class Weather(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val regionId: String? = null,
    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    val issuedAt: String? = null,
    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    val forecastTime: String? = null
)

@Document(indexName = "short_term_weather")
class ShortTermWeather(
    val category: String,
    val value: String,
    regionId: String,
    issuedAt: String,
    forecastTime: String
): Weather(regionId, issuedAt, forecastTime)

@Document(indexName = "ultra_short_term_weather")
class UltraShortTermWeather(
    val category: String,
    val value: String,
    regionId: String,
    issuedAt: String,
    forecastTime: String
): Weather(regionId, issuedAt, forecastTime)

class Test {
    fun test() {
        val shortTermWeather = ShortTermWeather("", "", "", "asdf", "asdf")

    }
}