package com.climacast.batch_server.model.repository

import com.climacast.batch_server.dto.HourlyWeatherUpsertRequestDTO
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
class WeatherJdbcRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
): WeatherJdbcRepository {

    companion object {
        const val HOURLY_WEATHER_UPSERT_QUERY = """
            INSERT INTO `climacast-weather`.Weather (
                parentRegion, childRegion, latitude, longitude, status, time, DTYPE, 
                weatherCode, temperature2m, temperature80m, temperature120m, temperature180m,
                windSpeed10m, windSpeed80m, windSpeed120m, windSpeed180m, humidity2m
            ) VALUES (?, ?, ?, ?, ?, ?, 'HOURLY', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                humidity2m = VALUES(humidity2m)
            """
        const val BATCH_SIZE = 100
    }

    override fun upsertHourlyWeatherForecasts(hourlyWeathers: Set<HourlyWeatherUpsertRequestDTO>) {
        jdbcTemplate.batchUpdate(HOURLY_WEATHER_UPSERT_QUERY, hourlyWeathers, BATCH_SIZE) { ps, weather ->
            ps.setString(1, weather.parentRegion)
            ps.setString(2, weather.childRegion)
            ps.setDouble(3, weather.latitude)
            ps.setDouble(4, weather.longitude)
            ps.setString(5, weather.status)
            ps.setTimestamp(6, Timestamp.valueOf(weather.time))
            ps.setInt(7, weather.weatherCode)
            ps.setDouble(8, weather.temperature2m)
            ps.setDouble(9, weather.temperature80m)
            ps.setDouble(10, weather.temperature120m)
            ps.setDouble(11, weather.temperature180m)
            ps.setDouble(12, weather.windSpeed10m)
            ps.setDouble(13, weather.windSpeed80m)
            ps.setDouble(14, weather.windSpeed120m)
            ps.setDouble(15, weather.windSpeed180m)
            ps.setInt(16, weather.humidity2m)
        }
    }
}