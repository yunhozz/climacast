package com.climacast.global.event.message

import com.climacast.global.dto.WeatherResponseDTO
import com.climacast.global.event.KafkaMessage

data class HistoryWeathersMessage(
    val data: List<WeatherResponseDTO>
) : KafkaMessage