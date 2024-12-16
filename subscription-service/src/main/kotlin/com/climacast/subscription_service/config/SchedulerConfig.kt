package com.climacast.subscription_service.config

import com.climacast.global.utils.logger
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar

@Configuration
@EnableScheduling
class SchedulerConfig : SchedulingConfigurer {

    private val log = logger()

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        val taskScheduler = ThreadPoolTaskScheduler().apply {
            threadNamePrefix = "SubscriptionScheduler-"
            poolSize = 10
            setErrorHandler {
                log.error("Scheduled Task Error : ${it.localizedMessage}", it)
            }
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(30)
            initialize()
        }
        taskRegistrar.setScheduler(taskScheduler)
    }
}