package com.climacast.batch_server.model.repository

import com.climacast.batch_server.dto.HourlyWeatherUpsertRequestDTO
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.LocalDateTime

@Repository
class WeatherJdbcRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
): WeatherJdbcRepository {

    companion object {
        const val HOURLY_WEATHER_UPSERT_QUERY = """
            INSERT INTO climacast_weather.Weather (
                parentRegion, childRegion, latitude, longitude, status, time, DTYPE, 
                weatherCode, temperature2m, temperature80m, temperature120m, temperature180m,
                windSpeed10m, windSpeed80m, windSpeed120m, windSpeed180m, humidity2m,
                createdAt, updatedAt
            ) VALUES (?, ?, ?, ?, ?, ?, 'HOURLY', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                parentRegion = VALUES(parentRegion),
                childRegion = VALUES(childRegion),
                status = VALUES(status),
                weatherCode = VALUES(weatherCode),
                temperature2m = VALUES(temperature2m),
                temperature80m = VALUES(temperature80m),
                temperature120m = VALUES(temperature120m),
                temperature180m = VALUES(temperature180m),
                windSpeed10m = VALUES(windSpeed10m),
                windSpeed80m = VALUES(windSpeed80m),
                windSpeed120m = VALUES(windSpeed120m),
                windSpeed180m = VALUES(windSpeed180m),
                humidity2m = VALUES(humidity2m),
                updatedAt =  VALUES(updatedAt)
            """
        const val BATCH_SIZE = 3000
    }

    override fun upsertHourlyWeatherForecasts(hourlyWeathers: Set<HourlyWeatherUpsertRequestDTO>) {
        jdbcTemplate.batchUpdate(HOURLY_WEATHER_UPSERT_QUERY, hourlyWeathers, BATCH_SIZE) { ps, weather ->
            ps.setString(1, weather.parentRegion)
            ps.setString(2, weather.childRegion)
            ps.setDouble(3, weather.latitude)
            ps.setDouble(4, weather.longitude)
            ps.setString(5, weather.status)
            ps.setTimestamp(6, Timestamp.valueOf(weather.time))
            weather.weatherCode?.let { ps.setInt(7, it) }
            weather.temperature2m?.let { ps.setDouble(8, it) }
            weather.temperature80m?.let { ps.setDouble(9, it) }
            weather.temperature120m?.let { ps.setDouble(10, it) }
            weather.temperature180m?.let { ps.setDouble(11, it) }
            weather.windSpeed10m?.let { ps.setDouble(12, it) }
            weather.windSpeed80m?.let { ps.setDouble(13, it) }
            weather.windSpeed120m?.let { ps.setDouble(14, it) }
            weather.windSpeed180m?.let { ps.setDouble(15, it) }
            weather.humidity2m?.let { ps.setInt(16, it) }
            ps.setTimestamp(17, Timestamp.valueOf(LocalDateTime.now()))
            ps.setTimestamp(18, Timestamp.valueOf(LocalDateTime.now()))
        }
    }
}