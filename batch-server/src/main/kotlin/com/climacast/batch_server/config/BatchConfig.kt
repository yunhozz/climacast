package com.climacast.batch_server.config

import com.climacast.batch_server.config.manager.OpenApiManager
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
    @Qualifier(AsyncConfig.TASK_EXECUTOR) private val taskExecutor: TaskExecutor,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val openApiManager: OpenApiManager
) {

    companion object {
        const val CSV_PATH = "/static/region-list.csv"
    }

    @Bean
    fun saveWeatherHistoryJob(): Job =
        JobBuilder("saveWeatherHistory", jobRepository)
            .start(saveWeatherHistoryOneDayAgoStep())
            .build()

    @Bean
    fun saveWeatherHistoryOneDayAgoStep(): Step =
        StepBuilder("test", jobRepository)
            .chunk<List<String>, List<WeatherResponseDTO>>(1, transactionManager)
            .reader(cityInfoReader())
            .processor(callOpenApi())
            .writer(saveWeathers())
            .taskExecutor(taskExecutor)
            .build()

    @Bean
    fun cityInfoReader(): ItemReader<List<String>> {
        val resource = ClassPathResource(CSV_PATH)
        val cityInfoList = Files.lines(resource.file.toPath()).toList()
        return IteratorItemReader(listOf(cityInfoList))
    }

    @Bean
    fun callOpenApi(): ItemProcessor<List<String>, List<WeatherResponseDTO>> =
        ItemProcessor { cities ->
            openApiManager.callOpenMeteoApi(cities)
        }

    @Bean
    fun saveWeathers(): ItemWriter<List<WeatherResponseDTO>> =
        ItemWriter { chunk ->
            chunk.forEach { responses ->
                openApiManager.saveWeathers(responses)
            }
        }
}