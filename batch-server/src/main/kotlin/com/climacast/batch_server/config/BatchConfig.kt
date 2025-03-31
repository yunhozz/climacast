package com.climacast.batch_server.config

import com.climacast.batch_server.common.api.OpenApiHandler
import com.climacast.batch_server.common.data.WeatherDataProcessor
import com.climacast.batch_server.common.enums.DailyConstants
import com.climacast.batch_server.common.enums.HourlyConstants
import com.climacast.batch_server.common.enums.WeatherParameters
import com.climacast.batch_server.common.message.WeatherDataKafkaSender
import com.climacast.batch_server.common.util.BatchDataBuffer
import com.climacast.batch_server.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.dto.Region
import com.climacast.global.dto.WeatherResponseDTO
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BatchConfig(
    private val batchJobRepository: JobRepository,
    private val batchTransactionManager: PlatformTransactionManager,
    private val appTransactionManager: PlatformTransactionManager,
    private val historyWeatherOpenApiHandler: OpenApiHandler<Region, WeatherResponseDTO>,
    private val forecastWeatherOpenApiHandler: OpenApiHandler<Region, WeatherResponseDTO>,
    private val weatherDataProcessor: WeatherDataProcessor,
    private val weatherDataKafkaSender: WeatherDataKafkaSender
) {
    companion object {
        const val CSV_PATH = "/static/region-list.csv"
        const val SAVE_WEATHER_HISTORY_JOB = "saveWeatherHistory"
        const val SAVE_WEATHER_FORECAST_JOB = "saveWeatherForecast"
        const val CALL_HISTORICAL_WEATHER_OPEN_API_STEP = "callHistoricalWeatherOpenApi"
        const val CALL_FORECAST_WEATHER_OPEN_API_STEP = "callForecastWeatherOpenApi"
        const val SAVE_WEATHER_DATA_STEP = "saveWeatherData"
        const val STEP_RETRY_COUNT = 3
    }

    @Bean
    fun saveWeatherHistoryJob(): Job =
        JobBuilder(SAVE_WEATHER_HISTORY_JOB, batchJobRepository)
            .incrementer(RunIdIncrementer())
            .start(callHistoricalWeatherOpenApiStep(""))
            .next(saveWeatherDataStep(""))
            .listener(jobCompletionListener())
            .build()

    @Bean
    fun saveWeatherForecastJob(): Job =
        JobBuilder(SAVE_WEATHER_FORECAST_JOB, batchJobRepository)
            .incrementer(RunIdIncrementer())
            .start(callForecastWeatherOpenApiStep(""))
            .next(saveWeatherDataStep(""))
            .listener(jobCompletionListener())
            .build()

    @Bean
    @JobScope
    fun callHistoricalWeatherOpenApiStep(@Value("#{jobParameters[chunkSize]}") chunkSize: String): Step =
        StepBuilder(CALL_HISTORICAL_WEATHER_OPEN_API_STEP, batchJobRepository)
            .chunk<Region, Region>(chunkSize.toInt(), batchTransactionManager)
            .reader(regionInfoReader())
            .writer(historicalWeatherOpenApiCallWriter(""))
            .build()

    @Bean
    @JobScope
    fun callForecastWeatherOpenApiStep(@Value("#{jobParameters[chunkSize]}") chunkSize: String): Step =
        StepBuilder(CALL_FORECAST_WEATHER_OPEN_API_STEP, batchJobRepository)
            .chunk<Region, Region>(chunkSize.toInt(), batchTransactionManager)
            .reader(regionInfoReader())
            .writer(forecastWeatherOpenApiCallWriter(""))
            .build()

    @Bean
    @JobScope
    fun saveWeatherDataStep(@Value("#{jobParameters[chunkSize]}") chunkSize: String): Step =
        StepBuilder(SAVE_WEATHER_DATA_STEP, batchJobRepository)
            .chunk<WeatherResponseDTO, WeatherResponseDTO>(chunkSize.toInt(), appTransactionManager)
            .reader(apiResponseReader())
            .writer(weatherDataWriter())
            .build()

    @Bean
    @JobScope
    fun jobCompletionListener() = object : JobExecutionListener {
        override fun afterJob(jobExecution: JobExecution) {
            BatchDataBuffer.clear()
        }
    }

    @Bean
    @StepScope
    fun regionInfoReader() = FlatFileItemReader<Region>().apply {
        setResource(ClassPathResource(CSV_PATH))
        setLineMapper(DefaultLineMapper<Region>().apply {
            setLineTokenizer(DelimitedLineTokenizer(",").apply {
                setNames("parent", "child", "latitude", "longitude")
            })
            setFieldSetMapper { fields ->
                Region(
                    fields.readString("parent"),
                    fields.readString("child"),
                    fields.readDouble("latitude"),
                    fields.readDouble("longitude")
                )
            }
        })
    }

    @Bean
    @StepScope
    fun historicalWeatherOpenApiCallWriter(@Value("#{jobParameters[weatherParam]}") weatherParam: String) = ItemWriter<Region> { chunk ->
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
        val data = historyWeatherOpenApiHandler
            .chunk(chunk)
            .query(dto)
            .callOpenApi()

        weatherDataKafkaSender.sendWeatherResponses(WeatherParameters.of(weatherParam), data)
        BatchDataBuffer.store(data)
    }

    @Bean
    @StepScope
    fun forecastWeatherOpenApiCallWriter(@Value("#{jobParameters[weatherParam]}") weatherParam: String) = ItemWriter<Region> { chunk ->
        val dto = OpenApiQueryRequestDTO(hourlyValues = HourlyConstants.ENTIRE)
        val data = forecastWeatherOpenApiHandler
            .chunk(chunk)
            .query(dto)
            .callOpenApi()

        weatherDataKafkaSender.sendWeatherResponses(WeatherParameters.of(weatherParam), data)
        BatchDataBuffer.store(data)
    }

    @Bean
    @StepScope
    fun apiResponseReader() = ItemReader<WeatherResponseDTO> { BatchDataBuffer.read() }

    @Bean
    @StepScope
    fun weatherDataWriter(): ItemWriter<WeatherResponseDTO> = ItemWriter { chunk ->
        weatherDataProcessor.process(chunk.items)
    }
}