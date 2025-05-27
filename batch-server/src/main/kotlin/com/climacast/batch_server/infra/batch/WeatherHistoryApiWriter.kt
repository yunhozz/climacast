package com.climacast.batch_server.infra.batch

import com.climacast.batch_server.common.enums.DailyConstants
import com.climacast.batch_server.common.enums.HourlyConstants
import com.climacast.batch_server.common.enums.WeatherParameters
import com.climacast.batch_server.common.util.WeatherDataBuffer
import com.climacast.batch_server.infra.api.HistoryWeatherOpenApiHandler
import com.climacast.batch_server.infra.message.WeatherDataKafkaSender
import com.climacast.batch_server.model.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.model.dto.Region
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class WeatherHistoryApiWriter(
    private val historyWeatherOpenApiHandler: HistoryWeatherOpenApiHandler,
    private val weatherDataKafkaSender: WeatherDataKafkaSender
) : ItemWriter<Region> {

    override fun write(chunk: Chunk<out Region>) {
        val dto = OpenApiQueryRequestDTO(
            dailyValues = listOf(
                DailyConstants.WEATHER_CODE,
                DailyConstants.TEMPERATURE_2M_MAX,
                DailyConstants.TEMPERATURE_2M_MIN,
                DailyConstants.TEMPERATURE_APPARENT_MAX,
                DailyConstants.TEMPERATURE_APPARENT_MIN,
                DailyConstants.SUNRISE,
                DailyConstants.SUNSET,
                DailyConstants.DAYLIGHT_DURATION,
                DailyConstants.SUNSHINE_DURATION,
                DailyConstants.RAIN_SUM,
                DailyConstants.SHOWERS_SUM,
                DailyConstants.SNOWFALL_SUM
            ),
            hourlyValues = HourlyConstants.ENTIRE
        )
        val data = historyWeatherOpenApiHandler.callOpenApi(chunk, dto)
        weatherDataKafkaSender.sendWeatherResponses(WeatherParameters.WEATHER_HISTORY, data)
        WeatherDataBuffer.store(data)
    }
}