package com.climacast.batch_server.config

import com.climacast.batch_server.common.enums.DailyConstants
import com.climacast.batch_server.common.enums.HourlyConstants
import com.climacast.batch_server.config.manager.OpenApiManager
import com.climacast.batch_server.config.manager.WeatherSaveManager
import com.climacast.batch_server.dto.OpenApiQueryRequestDTO
import com.climacast.batch_server.dto.WeatherResponseDTO
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.orm.jpa.JpaTransactionManager

data class Region(
    val parent: String,
    val child: String,
    val latitude: Double,
    val longitude: Double
)

@Configuration
@EnableBatchProcessing(
    dataSourceRef = "batchDataSource",
    transactionManagerRef = "batchTransactionManager"
)
class BatchConfig(
    private val batchJobRepository: JobRepository,
    private val batchTransactionManager: DataSourceTransactionManager,
    private val jpaTransactionManager: JpaTransactionManager,
    private val openApiManager: OpenApiManager,
    private val weatherSaveManager: WeatherSaveManager
) {

    companion object {
        const val CSV_PATH = "/static/region-list.csv"
        const val CHUNK_SIZE = 35
    }

    private val regions = arrayListOf<Region>()
    private val weatherResponseList = arrayListOf<WeatherResponseDTO>()

    @Bean
    fun saveWeatherHistoryJob(): Job =
        JobBuilder("saveWeatherHistory", batchJobRepository)
            .incrementer(RunIdIncrementer())
            .start(readCsvStep())
            .next(callHistoricalWeatherOpenApiStep())
            .next(saveWeathersOnMysqlStep())
            .build()

    @Bean
    fun saveWeatherForecastJob(): Job =
        JobBuilder("saveWeatherForecast", batchJobRepository)
            .incrementer(RunIdIncrementer())
            .start(readCsvStep())
            .next(callForecastWeatherOpenApiStep())
            .next(saveWeathersOnMysqlStep())
            .build()

    @Bean
    fun readCsvStep(): Step =
        StepBuilder("readCsv", batchJobRepository)
            .chunk<Region, Region>(CHUNK_SIZE, batchTransactionManager)
            .reader(regionInfoReader())
            .writer(regionInfoWriter())
            .build()

    @Bean
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
    fun regionInfoWriter() = ItemWriter<Region> { chunk ->
        regions.addAll(chunk)
    }

    @Bean
    fun callHistoricalWeatherOpenApiStep(): Step =
        StepBuilder("callHistoricalWeatherOpenApi", batchJobRepository)
            .chunk<WeatherResponseDTO, WeatherResponseDTO>(CHUNK_SIZE, batchTransactionManager)
            .reader(historicalWeatherOpenApiCallReader())
            .writer(weatherOpenApiResponseWriter())
            .build()

    @Bean
    fun callForecastWeatherOpenApiStep(): Step =
        StepBuilder("callForecastWeatherOpenApi", batchJobRepository)
            .chunk<WeatherResponseDTO, WeatherResponseDTO>(CHUNK_SIZE, batchTransactionManager)
            .reader(forecastWeatherOpenApiCallReader())
            .writer(weatherOpenApiResponseWriter())
            .build()

    @Bean
    fun historicalWeatherOpenApiCallReader() = object: ItemReader<WeatherResponseDTO> {
        private var iterator: Iterator<WeatherResponseDTO>? = null

        override fun read(): WeatherResponseDTO? {
            if (iterator == null) {
                val dto = OpenApiQueryRequestDTO(dailyValues = DailyConstants.ENTIRE)
                val responses = openApiManager.callHistoricalWeatherOpenApi(regions.toList(), dto)
                iterator = responses!!.iterator()
            }

            return if (iterator!!.hasNext()) iterator!!.next() else null
        }
    }

    @Bean
    fun forecastWeatherOpenApiCallReader() = object: ItemReader<WeatherResponseDTO> {
        private var iterator: Iterator<WeatherResponseDTO>? = null

        override fun read(): WeatherResponseDTO? {
            if (iterator == null) {
                val dto = OpenApiQueryRequestDTO(hourlyValues = HourlyConstants.ENTIRE)
                val responses = openApiManager.callForecastWeatherOpenApi(regions.toList(), dto)
                iterator = responses!!.iterator()
            }

            return if (iterator!!.hasNext()) iterator!!.next() else null
        }
    }

    @Bean
    fun weatherOpenApiResponseWriter() = ItemWriter<WeatherResponseDTO> { chunk ->
        weatherResponseList.addAll(chunk)
    }

    @Bean
    fun saveWeathersOnMysqlStep(): Step =
        StepBuilder("saveWeathersOnMysql", batchJobRepository)
            .chunk<WeatherResponseDTO, WeatherResponseDTO>(CHUNK_SIZE, jpaTransactionManager)
            .reader(apiResponseReader())
            .writer(weatherDataWriter())
            .build()

    @Bean
    fun apiResponseReader() = object: ItemReader<WeatherResponseDTO> {
        private var iterator: Iterator<WeatherResponseDTO>? = null

        override fun read(): WeatherResponseDTO? {
            if (iterator == null) {
                iterator = weatherResponseList.iterator()
            }

            return if (iterator!!.hasNext()) iterator!!.next() else null
        }
    }

    @Bean
    fun weatherDataWriter(): ItemWriter<WeatherResponseDTO> = ItemWriter { chunk ->
        weatherSaveManager.saveOnMysql(chunk.items)
    }
}