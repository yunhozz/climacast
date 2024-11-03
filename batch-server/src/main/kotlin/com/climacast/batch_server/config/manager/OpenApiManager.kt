package com.climacast.batch_server.config.manager

import com.climacast.batch_server.dto.WeatherResponseDTO

interface OpenApiManager {
    fun callOpenMeteoApi(cityInfoList: List<String>): MutableList<WeatherResponseDTO>?
    fun saveWeathers(responses: List<WeatherResponseDTO>)
}