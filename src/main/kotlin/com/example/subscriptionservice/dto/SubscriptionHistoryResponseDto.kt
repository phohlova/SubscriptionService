package com.example.subscriptionservice.dto

import com.example.subscriptionservice.domain.SubscriptionStatus
import java.time.LocalDateTime

data class SubscriptionHistoryResponseDto(
    val id: Long,
    val subscriptionId: Long,
    val oldStatus: SubscriptionStatus?,
    val newStatus: SubscriptionStatus,
    val changedAt: LocalDateTime
)