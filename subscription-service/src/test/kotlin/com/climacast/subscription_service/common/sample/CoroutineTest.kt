package com.climacast.subscription_service.common.sample

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

private val context = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

private fun testWithRunBlocking() = runBlocking {
    (1..10).forEach {
        launch(context) {
            runBlocking {
                println("Start No.$it in runBlocking on ${Thread.currentThread().name}")
                delay(1000)
                println("End No.$it in runBlocking on ${Thread.currentThread().name}")
            }
        }
    }
}

private fun testWithCoroutineScope() = runBlocking {
    (1..10).forEach {
        launch(context) {
            coroutineScope {
                println("Start No.$it in coroutineScope on ${Thread.currentThread().name}")
                delay(1000)
                println("End No.$it in coroutineScope on ${Thread.currentThread().name}")
            }
        }
    }
}

fun main() {
    testWithRunBlocking()
    testWithCoroutineScope()
}