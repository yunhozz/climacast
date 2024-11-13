package com.climacast.batch_server.service

import com.climacast.batch_server.config.BatchConfig
import com.climacast.global.utils.logger
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersInvalidException
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.repository.JobRestartException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class WeatherService(
    private val batchConfig: BatchConfig,
    private val jobLauncher: JobLauncher
) {

    private val log = logger()

    @Scheduled(cron = "0 1 0 * * *")
    fun saveWeatherHistoryEveryDay() {
        try {
            val jobExecution = jobLauncher.run(batchConfig.saveWeatherHistoryJob(), createJobParameters())
            log.info(createLogMessage(jobExecution))
        } catch (e: Exception) {
            when (e) {
                is JobExecutionAlreadyRunningException,
                is JobInstanceAlreadyCompleteException,
                is JobParametersInvalidException,
                is JobRestartException
                    -> log.error(e.message)
                else -> throw e
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    fun saveWeatherForecastEveryHour() {
        try {
            val jobExecution = jobLauncher.run(batchConfig.saveWeatherForecastJob(), createJobParameters())
            log.info(createLogMessage(jobExecution))
        } catch (e: Exception) {
            when (e) {
                is JobExecutionAlreadyRunningException,
                is JobInstanceAlreadyCompleteException,
                is JobParametersInvalidException,
                is JobRestartException
                    -> log.error(e.message)
                else -> throw e
            }
        }
    }

    private fun createJobParameters(): JobParameters {
        val parameters = mapOf(
            "uuid" to JobParameter(UUID.randomUUID().toString(), String::class.java),
            "time" to JobParameter(System.currentTimeMillis(), Long::class.java)
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