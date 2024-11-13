package com.climacast.batch_server.model.entity

import com.climacast.batch_server.common.enums.WeatherStatus
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import org.springframework.data.domain.Persistable
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
): BaseEntity(), Persistable<WeatherId> {
    @EmbeddedId
    val weatherId = WeatherId(childRegion, time)

    override fun getId(): WeatherId? = this.weatherId

    override fun isNew(): Boolean {
        return super.createdAt == null
    }
}

@Embeddable
data class WeatherId(
    val childRegion: String,
    val time: LocalDateTime
)