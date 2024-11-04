package com.climacast.batch_server.config

import com.climacast.batch_server.config.manager.OpenApiManager
import com.climacast.batch_server.config.manager.WeatherSaveManager
import com.climacast.batch_server.dto.WeatherResponseDTO
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.IteratorItemReader
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import java.nio.file.Files

@Configuration
@EnableBatchProcessing
class BatchConfig(
    @Qualifier(AsyncConfig.TASK_EXECUTOR)
    private val taskExecutor: TaskExecutor,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val openApiManager: OpenApiManager,
    private val weatherSaveManager: WeatherSaveManager
) {

    companion object {
        const val CSV_PATH = "/static/region-list.csv"
    }

    @Bean
    fun saveWeatherJob(): Job =
        JobBuilder("saveWeathers", jobRepository)
            .start(saveWeatherHistoryOneDayAgoStep())
            .build()

    @Bean
    fun saveWeatherHistoryOneDayAgoStep(): Step =
        StepBuilder("saveWeatherHistoryOneDayAgo", jobRepository)
            .chunk<List<String>, List<WeatherResponseDTO>>(1, transactionManager)
            .reader(readCityInfoList())
            .processor(callHistoricalWeatherOpenApi())
            .writer(saveWeathers())
            .taskExecutor(taskExecutor)
            .build()

    @Bean
    fun readCityInfoList(): ItemReader<List<String>> {
        val resource = ClassPathResource(CSV_PATH)
        val cityInfoList = Files.lines(resource.file.toPath()).toList()
        return IteratorItemReader(listOf(cityInfoList))
    }

    @Bean
    fun callHistoricalWeatherOpenApi(): ItemProcessor<List<String>, List<WeatherResponseDTO>> =
        ItemProcessor { cities ->
            openApiManager.callHistoricalWeatherOpenApi(cities)
        }

    @Bean
    fun saveWeathers(): ItemWriter<List<WeatherResponseDTO>> =
        ItemWriter { chunk ->
            chunk.forEach { responses ->
                weatherSaveManager.saveOnElasticsearch(responses)
            }
        }
}