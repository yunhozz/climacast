package com.climacast.batch_server.service

import com.climacast.batch_server.config.BatchConfig
import com.climacast.global.utils.logger
import org.springframework.batch.core.JobExecution
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

    private val log = logger()

    @Scheduled(cron = "0 0 0 * * *")
    fun saveWeatherHistoryEveryDay() {
        val jobExecution = jobLauncher.run(batchConfig.saveWeatherHistoryJob(), createJobParameters())
        log.info(createLogMessage(jobExecution))
    }

    @Scheduled(cron = "0 0 * * * *")
    fun saveWeatherForecastEveryHour() {
        val jobExecution = jobLauncher.run(batchConfig.saveWeatherForecastJob(), createJobParameters())
        log.info(createLogMessage(jobExecution))
    }

    private fun createJobParameters(): JobParameters {
        val parameters = mapOf(
            "time" to JobParameter(Date(), Date::class.java)
        )
        return JobParameters(parameters)
    }

    companion object {
        private fun createLogMessage(jobExecution: JobExecution) = StringBuilder().apply {
            append("Job Execution: ").append(jobExecution.status).append("\n")
            append("Job getJobId: ").append(jobExecution.jobId).append("\n")
            append("Job getJobName: ").append(jobExecution.jobInstance.jobName).append("\n")
            append("Job getExitStatus: ").append(jobExecution.exitStatus).append("\n")
            append("Job getJobInstance: ").append(jobExecution.jobInstance).append("\n")
            append("Job getStepExecutions: ").append(jobExecution.stepExecutions).append("\n")
            append("Job getLastUpdated: ").append(jobExecution.lastUpdated).append("\n")
            append("Job getFailureExceptions: ").append(jobExecution.failureExceptions).append("\n")
        }.toString()
    }
}