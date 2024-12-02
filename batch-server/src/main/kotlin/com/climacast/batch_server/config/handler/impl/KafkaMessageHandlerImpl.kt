package com.climacast.batch_server.config.handler.impl

import com.climacast.batch_server.common.enums.WeatherParameters
import com.climacast.batch_server.config.handler.KafkaMessageHandler
import com.climacast.batch_server.dto.WeatherResponseDTO
import com.climacast.global.dto.KafkaEvent
import com.climacast.global.enums.KafkaTopic
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaMessageHandlerImpl(
    private val kafkaTemplate: KafkaTemplate<String, Any>
): KafkaMessageHandler {

    override fun sendWeatherResponses(param: WeatherParameters, weatherData: List<WeatherResponseDTO>) {
        weatherData.chunked(weatherData.size / 3).forEach { data ->
            val event = createKafkaEvent(param, data)
            kafkaTemplate.send(event.topic, event.message)
        }
    }

    private fun createKafkaEvent(param: WeatherParameters, weatherData: List<WeatherResponseDTO>): KafkaEvent =
        when (param) {
            WeatherParameters.WEATHER_FORECAST ->
                KafkaEvent(
                    topic = KafkaTopic.WEATHER_FORECAST_TOPIC,
                    message = weatherData
                )
            WeatherParameters.WEATHER_HISTORY ->
                KafkaEvent(
                    topic = KafkaTopic.WEATHER_HISTORY_TOPIC,
                    message = weatherData
                )
        }
}