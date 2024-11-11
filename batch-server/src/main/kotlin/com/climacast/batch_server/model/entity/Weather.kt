package com.climacast.batch_server.model.entity

import com.climacast.batch_server.common.enums.WeatherStatus
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import java.time.LocalDateTime

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class Weather(
    val parentRegion: String,
    childRegion: String,
    val latitude: Double,
    val longitude: Double,
    @Enumerated(EnumType.STRING)
    val status: WeatherStatus,
    time: LocalDateTime
) {
    @EmbeddedId
    val id: WeatherId = WeatherId(childRegion, time)
}

@Embeddable
data class WeatherId(
    val childRegion: String,
    val time: LocalDateTime
)