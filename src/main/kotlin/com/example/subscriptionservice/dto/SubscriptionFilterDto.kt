package com.example.subscriptionservice.dto

import com.example.subscriptionservice.domain.SubscriptionStatus
import java.time.LocalDateTime

data class SubscriptionFilterDto(
    val userId: Long? = null,
    val serviceName: String? = null,
    val status: SubscriptionStatus? = null,
    val startDateFrom: LocalDateTime? = null,
    val startDateTo: LocalDateTime? = null,
    val endDateFrom: LocalDateTime? = null,
    val endDateTo: LocalDateTime? = null
)