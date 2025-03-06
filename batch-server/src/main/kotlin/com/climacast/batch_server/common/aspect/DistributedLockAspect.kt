package com.climacast.batch_server.common.aspect

import com.climacast.batch_server.common.annotation.DistributedLock
import com.climacast.global.utils.logger
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

    @Around("@annotation(com.climacast.batch_server.common.annotation.DistributedLock)")
    fun distributedLock(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val distributedLock = method.getAnnotation(DistributedLock::class.java)

        val lockKey = distributedLock.key
        val rLock = redissonClient.getLock(lockKey)

        return try {
            val lockable = rLock.tryLock(distributedLock.waitTime, distributedLock.leaseTime, TimeUnit.SECONDS)
            if (!lockable) {
                log.info("Failed to acquire lock for key : $lockKey")
                null
            } else joinPoint.proceed()
        } finally {
            try {
                rLock.unlock()
            } catch (e: Exception) {
                log.info("Redisson lock already unlocked : $lockKey")
            }
        }
    }
}