package com.climacast.subscription_service.common.aspect

import com.climacast.global.utils.logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component

@Component
@Aspect
class SchedulerLoggingAspect {

    private val log = logger()

    @Around("execution(* com.climacast.subscription_service.service.SubscriptionScheduleService.*(..))")
    fun logSubscriptionScheduler(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val schedulerName = signature.name

        log.info(">>>>> Scheduler $schedulerName started")

        return try {
            val result = joinPoint.proceed()
            log.info("<<<<< Scheduler $schedulerName completed successfully")
            result

        } catch (e: Exception) {
            when (e) {
                is InterruptedException, is IllegalMonitorStateException ->
                    log.info("<<<<< Failed to run scheduler: $schedulerName")
                else ->
                    log.error(e.localizedMessage, e)
            }
            null
        }
    }
}