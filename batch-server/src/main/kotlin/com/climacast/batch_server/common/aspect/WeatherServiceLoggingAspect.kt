package com.climacast.batch_server.common.aspect

import com.climacast.global.utils.logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.batch.core.JobParametersInvalidException
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.repository.JobRestartException
import org.springframework.stereotype.Component

@Component
@Aspect
class WeatherServiceLoggingAspect {

    private val log = logger()

    @Around("execution(* com.climacast.batch_server.service.WeatherService.*(..))")
    fun around(point: ProceedingJoinPoint): Any? {
        val methodName = point.signature.name
        return try {
            log.info(">>>>> Scheduling Start : $methodName")
            point.proceed()
            log.info("<<<<< Scheduling Success : $methodName")
        } catch (e: Exception) {
            when (e) {
                is JobExecutionAlreadyRunningException,
                is JobInstanceAlreadyCompleteException,
                is JobParametersInvalidException,
                is JobRestartException
                    -> log.error("<<<<< Scheduling Failed : $methodName", e.localizedMessage)
                else -> log.error(e.message)
            }
        }
    }
}