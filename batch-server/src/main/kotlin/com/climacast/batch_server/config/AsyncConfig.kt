package com.climacast.batch_server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class AsyncConfig {

    companion object {
        const val TASK_EXECUTOR = "taskExecutor"
    }

    @Bean(TASK_EXECUTOR)
    fun taskExecutor(): TaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 10
            maxPoolSize = 20
            queueCapacity = 50
            setAllowCoreThreadTimeOut(true) // core thread 가 일정시간 태스크를 받지 않을 경우 pool 에서 정리
            initialize()
        }
}