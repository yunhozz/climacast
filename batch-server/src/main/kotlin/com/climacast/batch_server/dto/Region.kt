package com.climacast.batch_server.dto

data class Region(
    val parent: String,
    val child: String,
    val latitude: Double,
    val longitude: Double
)