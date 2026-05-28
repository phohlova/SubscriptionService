package com.example.subscriptionservice.specification

import com.example.subscriptionservice.domain.entity.Subscription
import com.example.subscriptionservice.domain.SubscriptionStatus
import com.example.subscriptionservice.dto.SubscriptionFilterDto
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

object SubscriptionSpecification {

    fun toSpecification(filter: SubscriptionFilterDto): Specification<Subscription> {
        return Specification { root, query, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            if (filter.userId != null) {
                predicates.add(criteriaBuilder.equal(root.get<Long>("userId"), filter.userId))
            }

            if (filter.serviceName != null) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get<String>("serviceName")),
                        "%${filter.serviceName.lowercase()}%"
                    )
                )
            }

            if (filter.status != null) {
                predicates.add(criteriaBuilder.equal(root.get<SubscriptionStatus>("status"), filter.status))
            }

            if (filter.startDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get<LocalDateTime>("startDate"), filter.startDateFrom))
            }
            if (filter.startDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get<LocalDateTime>("startDate"), filter.startDateTo))
            }

            if (filter.endDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get<LocalDateTime>("endDate"), filter.endDateFrom))
            }
            if (filter.endDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get<LocalDateTime>("endDate"), filter.endDateTo))
            }

            if (predicates.isEmpty()) {
                null
            } else {
                criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
    }
}