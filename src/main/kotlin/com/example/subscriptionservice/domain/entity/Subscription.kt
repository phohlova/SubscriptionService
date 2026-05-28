package com.example.subscriptionservice.domain.entity

import com.example.subscriptionservice.domain.SubscriptionStatus
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "subscriptions")
@JsonIgnoreProperties(ignoreUnknown = true)
class Subscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "service_name", nullable = false, length = 255)
    var serviceName: String,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: SubscriptionStatus = SubscriptionStatus.ACTIVE,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDateTime,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDateTime,

    @Column(name = "cost", precision = 19, scale = 2, nullable = false)
    var cost: BigDecimal,

    @Column(name = "auto_renew", nullable = false)
    var autoRenew: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
) {
    protected constructor() : this(
        id = null,
        userId = 0,
        serviceName = "",
        status = SubscriptionStatus.ACTIVE,
        startDate = LocalDateTime.MIN,
        endDate = LocalDateTime.MIN,
        cost = BigDecimal.ZERO,
        autoRenew = false,
        createdAt = null,
        updatedAt = null
    )
}