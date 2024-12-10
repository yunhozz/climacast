package com.climacast.batch_server.common.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val leaseTime: Long,
    val waitTime: Long
)