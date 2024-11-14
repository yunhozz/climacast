package com.climacast.global

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GlobalApplication

fun main(args: Array<String>) {
	runApplication<GlobalApplication>(*args)
}
