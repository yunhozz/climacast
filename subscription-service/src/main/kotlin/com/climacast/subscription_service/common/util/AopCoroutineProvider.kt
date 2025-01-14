package com.climacast.subscription_service.common.util

import org.aspectj.lang.ProceedingJoinPoint
import kotlin.coroutines.Continuation
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

fun ProceedingJoinPoint.runCoroutine(block: suspend () -> Any?): Any =
    block.startCoroutine(this.coroutineContinuation())

suspend fun ProceedingJoinPoint.proceedCoroutine(args: Array<Any?> = this.coroutineArgs()): Any? =
    suspendCoroutine { continuation ->
        this.proceed(args + continuation)
    }

@Suppress("UNCHECKED_CAST")
private fun ProceedingJoinPoint.coroutineContinuation(): Continuation<Any?> =
    this.args.last() as Continuation<Any?>

private fun ProceedingJoinPoint.coroutineArgs(): Array<Any?> =
    this.args.sliceArray(0 until this.args.size - 1)