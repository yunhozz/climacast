package com.climacast.batch_server.service

import com.climacast.batch_server.config.BatchConfig
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.Date

@Service
class WeatherService(
    private val batchConfig: BatchConfig,
    private val jobLauncher: JobLauncher
) {

    @Scheduled(cron = "0 0 0 * * *")
    fun saveWeatherHistoryEveryDay() {
        jobLauncher.run(batchConfig.saveWeatherHistoryJob(), createJobParameters())
    }

    private fun createJobParameters(): JobParameters {
        val parameters = mapOf(
            "time" to JobParameter(Date(), Date::class.java)
        )
        return JobParameters(parameters)
    }
}