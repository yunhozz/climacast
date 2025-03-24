package com.climacast.subscription_service.common.aspect

import com.climacast.global.utils.logger
import com.climacast.subscription_service.common.annotation.DistributedLock
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Aspect
class DistributedLockAspect(
    private val redissonClient: RedissonClient
) {
    private val log = logger()

    @Around("@annotation(com.climacast.subscription_service.common.annotation.DistributedLock)")
    fun lockWithRedis(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val distributedLock = method.getAnnotation(DistributedLock::class.java)

        val lockKey = distributedLock.key
        val rLock = redissonClient.getLock(lockKey)

        return try {
            val lockable = rLock.tryLock(distributedLock.waitTime, distributedLock.leaseTime, TimeUnit.SECONDS)
            if (!lockable) {
                log.info("Failed to acquire lock for key: $lockKey")
                null
            } else {
                log.info("Acquire distributed lock for key: $lockKey")
                joinPoint.proceed()
            }

        } catch (e: Exception) {
            when (e) {
                is InterruptedException, is IllegalMonitorStateException ->
                    log.info("Failed to acquire lock for key: $lockKey", e)
                else ->
                    log.error(e.localizedMessage, e)
            }
            null

        } finally {
            if (rLock != null && rLock.isLocked) rLock.unlock()
        }
    }
}