package com.climacast.batch_server.service

import com.climacast.batch_server.common.annotation.DistributedLock
import com.climacast.batch_server.common.enums.WeatherParameters
import com.climacast.batch_server.config.BatchConfig
import com.climacast.global.utils.logger
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class WeatherSchedulerService(
    private val batchConfig: BatchConfig,
    private val jobLauncher: JobLauncher
) {
    private val log = logger()

    @Scheduled(cron = "0 28,58 * * * *")
    @DistributedLock(key = WEATHER_FORECAST_JOB_LOCK_KEY, leaseTime = 60, waitTime = 0)
    fun saveWeatherForecastEveryHour() {
        val jobParameters = createDefaultJobParameters()
            .addString("weatherParam", WeatherParameters.WEATHER_FORECAST.name)
            .toJobParameters()
        val jobExecution = jobLauncher.run(batchConfig.weatherForecastJob(), jobParameters)
        log.info(createLogMessage(jobExecution))
    }

    @Scheduled(cron = "0 3 0 * * *", zone = "Asia/Seoul")
    @DistributedLock(key = WEATHER_HISTORY_JOB_LOCK_KEY, leaseTime = 30, waitTime = 0)
    fun saveWeatherHistoryEveryDay() {
        val jobParameters = createDefaultJobParameters()
            .addString("weatherParam", WeatherParameters.WEATHER_HISTORY.name)
            .toJobParameters()
        val jobExecution = jobLauncher.run(batchConfig.weatherHistoryJob(), jobParameters)
        log.info(createLogMessage(jobExecution))
    }

    private fun createDefaultJobParameters() = JobParametersBuilder()
        .addJobParameters(
            JobParameters(mapOf(
                "uuid" to JobParameter(UUID.randomUUID().toString(), String::class.java),
                "time" to JobParameter(System.currentTimeMillis(), Long::class.java)
            ))
        )

    companion object {
        const val WEATHER_FORECAST_JOB_LOCK_KEY = "weather-forecast-job-lock"
        const val WEATHER_HISTORY_JOB_LOCK_KEY = "weather-history-job-lock"

        fun createLogMessage(jobExecution: JobExecution) = buildString {
            append("Job Execution: ").append(jobExecution.status).append("\n")
            append("Job Id: ").append(jobExecution.jobId).append("\n")
            append("Job Name: ").append(jobExecution.jobInstance.jobName).append("\n")
            append("Job Exit Status: ").append(jobExecution.exitStatus).append("\n")
            append("Job Instance: ").append(jobExecution.jobInstance).append("\n")
            append("Job Step Executions: ").append(jobExecution.stepExecutions).append("\n")
            append("Job Last Updated: ").append(jobExecution.lastUpdated).append("\n")
            append("Job Failure Exceptions: ").append(jobExecution.failureExceptions).append("\n")
        }
    }
}