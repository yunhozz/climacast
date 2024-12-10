package com.climacast.batch_server.config

import com.climacast.global.utils.logger
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar

@Configuration
@EnableScheduling
class SchedulerConfig: SchedulingConfigurer {

    private val log = logger()

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        val taskScheduler = ThreadPoolTaskScheduler().apply {
            threadNamePrefix = "SchedulerThread-"
            poolSize = 3
            setErrorHandler {
                log.error("Scheduled Task Error : ${it.localizedMessage}", it)
            }
            setWaitForTasksToCompleteOnShutdown(true) // 어플리케이션 종료 시 대기
            setAwaitTerminationSeconds(30) // 최대 대기 시간
            initialize()
        }
        taskRegistrar.setScheduler(taskScheduler)
    }
}