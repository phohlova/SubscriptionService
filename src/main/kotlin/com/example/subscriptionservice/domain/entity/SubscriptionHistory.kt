package com.example.subscriptionservice.domain.entity

import com.example.subscriptionservice.domain.SubscriptionStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "subscription_history")
class SubscriptionHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    var subscription: Subscription? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    var oldStatus: SubscriptionStatus? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    var newStatus: SubscriptionStatus = SubscriptionStatus.ACTIVE,

    @Column(name = "changed_at", nullable = false)
    var changedAt: LocalDateTime = LocalDateTime.now()
) {
    protected constructor() : this(
        id = null,
        subscription = null,
        oldStatus = null,
        newStatus = SubscriptionStatus.ACTIVE,
        changedAt = LocalDateTime.now()
    )
}