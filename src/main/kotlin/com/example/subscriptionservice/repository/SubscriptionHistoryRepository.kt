package com.example.subscriptionservice.repository

import com.example.subscriptionservice.domain.entity.SubscriptionHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionHistoryRepository : JpaRepository<SubscriptionHistory, Long> {

    fun findBySubscriptionIdOrderByChangedAtDesc(subscriptionId: Long): List<SubscriptionHistory>
}