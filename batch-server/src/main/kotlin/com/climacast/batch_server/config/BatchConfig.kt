package com.climacast.batch_server.config

import com.climacast.batch_server.common.util.WeatherDataBuffer
import com.climacast.batch_server.infra.batch.BatchConstants
import com.climacast.batch_server.infra.batch.RegionCsvReader
import com.climacast.batch_server.infra.batch.WeatherForecastApiWriter
import com.climacast.batch_server.infra.batch.WeatherHistoryApiWriter
import com.climacast.batch_server.infra.data.WeatherDataProcessor
import com.climacast.batch_server.model.dto.Region
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BatchConfig(
    private val batchJobRepository: JobRepository,
    private val batchTransactionManager: PlatformTransactionManager,
    private val appTransactionManager: PlatformTransactionManager,
    private val regionCsvReader: RegionCsvReader,
    private val weatherForecastApiWriter: WeatherForecastApiWriter,
    private val weatherHistoryApiWriter: WeatherHistoryApiWriter,
    private val weatherDataProcessor: WeatherDataProcessor
) {
    @Bean
    fun weatherForecastJob(): Job =
        JobBuilder(BatchConstants.WEATHER_FORECAST_JOB, batchJobRepository)
            .incrementer(RunIdIncrementer())
            .start(callForecastWeatherOpenApiStep())
            .next(saveWeatherDataStep())
            .listener(jobCompletionListener())
            .build()

    @Bean
    fun weatherHistoryJob(): Job =
        JobBuilder(BatchConstants.WEATHER_HISTORY_JOB, batchJobRepository)
            .incrementer(RunIdIncrementer())
            .start(callHistoricalWeatherOpenApiStep())
            .next(saveWeatherDataStep())
            .listener(jobCompletionListener())
            .build()

    @Bean
    @JobScope
    fun callForecastWeatherOpenApiStep(): Step =
        StepBuilder(BatchConstants.CALL_FORECAST_WEATHER_OPEN_API_STEP, batchJobRepository)
            .chunk<Region, Region>(BatchConstants.WEATHER_API_CHUNK_SIZE, batchTransactionManager)
            .reader(regionCsvReader)
            .writer(weatherForecastApiWriter)
            .build()

    @Bean
    @JobScope
    fun callHistoricalWeatherOpenApiStep(): Step =
        StepBuilder(BatchConstants.CALL_HISTORICAL_WEATHER_OPEN_API_STEP, batchJobRepository)
            .chunk<Region, Region>(BatchConstants.WEATHER_API_CHUNK_SIZE, batchTransactionManager)
            .reader(regionCsvReader)
            .writer(weatherHistoryApiWriter)
            .build()

    @Bean
    @JobScope
    fun saveWeatherDataStep(): Step =
        StepBuilder(BatchConstants.SAVE_WEATHER_DATA_STEP, batchJobRepository)
            .tasklet({ contribution, chunkContext ->
                val data = WeatherDataBuffer.readAll()
                weatherDataProcessor.process(data)
                RepeatStatus.FINISHED
            }, appTransactionManager)
            .build()

    @Bean
    @JobScope
    fun jobCompletionListener() = object : JobExecutionListener {
        override fun afterJob(jobExecution: JobExecution) {
            WeatherDataBuffer.clear()
        }
    }
}