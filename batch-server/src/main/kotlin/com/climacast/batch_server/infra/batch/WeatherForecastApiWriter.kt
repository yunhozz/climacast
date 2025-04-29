package com.climacast.batch_server.infra.batch

import com.climacast.batch_server.common.enums.HourlyConstants
import com.climacast.batch_server.common.enums.WeatherParameters
import com.climacast.batch_server.common.util.WeatherDataBuffer
import com.climacast.batch_server.infra.api.ForecastWeatherOpenApiHandler
import com.climacast.batch_server.infra.message.WeatherDataKafkaSender
import com.climacast.batch_server.model.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.model.dto.Region
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class WeatherForecastApiWriter(
    private val forecastWeatherOpenApiHandler: ForecastWeatherOpenApiHandler,
    private val weatherDataKafkaSender: WeatherDataKafkaSender
) : ItemWriter<Region> {

    override fun write(chunk: Chunk<out Region>) {
        val dto = OpenApiQueryRequestDTO(hourlyValues = HourlyConstants.ENTIRE)
        val data = forecastWeatherOpenApiHandler.callOpenApi(chunk, dto)
        weatherDataKafkaSender.sendWeatherResponses(WeatherParameters.WEATHER_FORECAST, data)
        WeatherDataBuffer.store(data)
    }
}