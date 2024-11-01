package com.climacast.batch_server.dto

data class GeocodingResponseDTO(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String
)