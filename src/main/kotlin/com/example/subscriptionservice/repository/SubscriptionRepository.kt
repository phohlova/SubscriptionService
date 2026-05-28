package com.example.subscriptionservice.repository

import com.example.subscriptionservice.domain.Subscription
import com.example.subscriptionservice.domain.SubscriptionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {

    fun findByUserId(userId: Long, pageable: Pageable): Page<Subscription>

    fun findByUserIdAndStatus(userId: Long, status: SubscriptionStatus, pageable: Pageable): Page<Subscription>

    fun findByServiceName(serviceName: String, pageable: Pageable): Page<Subscription>

    fun findByStatus(status: SubscriptionStatus, pageable: Pageable): Page<Subscription>

    @Query("SELECT s FROM Subscription s WHERE s.startDate BETWEEN :startDateFrom AND :startDateTo")
    fun findByStartDateBetween(
        @Param("startDateFrom") startDateFrom: LocalDateTime,
        @Param("startDateTo") startDateTo: LocalDateTime,
        pageable: Pageable
    ): Page<Subscription>

    @Query("SELECT s FROM Subscription s WHERE s.endDate BETWEEN :endDateFrom AND :endDateTo")
    fun findByEndDateBetween(
        @Param("endDateFrom") endDateFrom: LocalDateTime,
        @Param("endDateTo") endDateTo: LocalDateTime,
        pageable: Pageable
    ): Page<Subscription>

    @Query("SELECT s FROM Subscription s WHERE s.endDate < :currentTime AND s.status = 'ACTIVE'")
    fun findExpiredSubscriptions(@Param("currentTime") currentTime: LocalDateTime): List<Subscription>
}