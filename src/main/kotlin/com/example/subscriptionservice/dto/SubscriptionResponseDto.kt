package com.example.subscriptionservice.dto

import com.example.subscriptionservice.domain.SubscriptionStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class SubscriptionResponseDto(
    val id: Long,
    val userId: Long,
    val serviceName: String,
    val status: SubscriptionStatus,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val cost: BigDecimal,
    val autoRenew: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)