package com.example.subscriptionservice.dto

import com.example.subscriptionservice.domain.SubscriptionStatus

data class SubscriptionUpdateStatusDto(
    val status: SubscriptionStatus
)