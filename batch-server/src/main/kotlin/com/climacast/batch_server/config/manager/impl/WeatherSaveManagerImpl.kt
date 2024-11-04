package com.climacast.batch_server.config.manager.impl

import com.climacast.batch_server.config.manager.WeatherSaveManager
import com.climacast.batch_server.dto.WeatherResponseDTO
import com.climacast.batch_server.model.document.WeatherData
import com.climacast.batch_server.model.document.WeatherDocument
import com.climacast.batch_server.model.repository.WeatherDocumentRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.UUID

@Component
class WeatherSaveManagerImpl(
    private val weatherDocumentRepository: WeatherDocumentRepository
): WeatherSaveManager {

    override fun saveOnElasticsearch(weathers: List<WeatherResponseDTO>) {
        Flux.fromIterable(weathers)
            .flatMap { weather ->
                val hourly = weather.hourly
                val weatherDocuments = hourly.time.zip(hourly.temperature_2m) { time, temperature ->
                    WeatherDocument(
                        UUID.randomUUID().toString(),
                        weather.parentRegion,
                        weather.childRegion,
                        weather.latitude,
                        weather.longitude,
                        time,
                        WeatherData(temperature)
                    )
                }
                Flux.fromIterable(weatherDocuments)
            }
            .collectList()
            .flatMap { weatherDocuments ->
                Mono.fromCallable {
                    weatherDocumentRepository.saveAll(weatherDocuments)
                }.subscribeOn(Schedulers.boundedElastic())
            }
            .subscribe()
    }

    override fun saveOnMysql(weathers: List<WeatherResponseDTO>) {
        TODO("Not yet implemented")
    }
}