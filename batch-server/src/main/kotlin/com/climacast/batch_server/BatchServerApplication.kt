package com.climacast.batch_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class BatchServerApplication

fun main(args: Array<String>) {
	runApplication<BatchServerApplication>(*args)
}
