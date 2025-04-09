package com.climacast.global.event.message

import com.climacast.global.dto.WeatherResponseDTO
import com.climacast.global.event.KafkaMessage

data class ForecastWeathersMessage(
    val data: List<WeatherResponseDTO>
) : KafkaMessage