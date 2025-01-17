package com.climacast.subscription_service.config

import com.climacast.global.utils.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {

    private val log = logger()

    companion object {
        const val THREAD_COUNT = 8
        const val SIMULTANEOUS_CONNECTION_COUNT = 100
        const val QUEUE_CAPACITY = 50
    }

    @Bean
    override fun getAsyncExecutor(): Executor? {
        val executor = ThreadPoolTaskExecutor().apply {
            threadNamePrefix = "SubscriptionThread-"
            corePoolSize = THREAD_COUNT
            queueCapacity = QUEUE_CAPACITY
            maxPoolSize = THREAD_COUNT * (SIMULTANEOUS_CONNECTION_COUNT - 1) + (THREAD_COUNT * 2)
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        }
        log.info("Will use {}/{} thread pool", executor.corePoolSize, executor.maxPoolSize);
        executor.initialize()

        return executor
    }
}