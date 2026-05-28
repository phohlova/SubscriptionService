package com.example.subscriptionservice.domain

import com.example.subscriptionservice.domain.SubscriptionStatus
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "subscriptions")
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
)