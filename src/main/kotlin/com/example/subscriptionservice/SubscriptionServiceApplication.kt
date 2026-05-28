package com.example.subscriptionservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
open class SubscriptionServiceApplication

fun main(args: Array<String>) {
    runApplication<SubscriptionServiceApplication>(*args)
}
