package com.example.subscriptionservice.repository

import com.example.subscriptionservice.domain.entity.Subscription
import com.example.subscriptionservice.domain.SubscriptionStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDateTime

@DataJpaTest
@DisplayName("SubscriptionRepository — Тесты с H2")
open class SubscriptionRepositoryTest {

    @Autowired
    private lateinit var subscriptionRepository: SubscriptionRepository

    @BeforeEach
    fun setUp() {
        subscriptionRepository.deleteAll()
    }

    @Test
    @DisplayName("save and findById")
    fun `save and findById should work correctly`() {
        // Arrange
        val subscription = Subscription(
            userId = 1L,
            serviceName = "Test Service",
            status = SubscriptionStatus.ACTIVE,
            startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
            endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
            cost = BigDecimal("9.99"),
            autoRenew = true
        )

        // Act
        val saved = subscriptionRepository.save(subscription)
        val found = subscriptionRepository.findById(saved.id!!)

        // Assert
        assertThat(found).isPresent
        assertThat(found.get().serviceName).isEqualTo("Test Service")
        assertThat(found.get().status).isEqualTo(SubscriptionStatus.ACTIVE)
        assertThat(found.get().userId).isEqualTo(1L)
    }

    @Test
    @DisplayName("findByUserId — фильтрация по пользователю")
    fun `findByUserId should return subscriptions for user`() {
        // Arrange
        subscriptionRepository.save(
            Subscription(
                userId = 1L,
                serviceName = "Test Service",
                status = SubscriptionStatus.ACTIVE,
                startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
                endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
                cost = BigDecimal("9.99"),
                autoRenew = true
            )
        )
        subscriptionRepository.save(
            Subscription(
                userId = 2L,
                serviceName = "Other Service",
                status = SubscriptionStatus.ACTIVE,
                startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
                endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
                cost = BigDecimal("5.99"),
                autoRenew = false
            )
        )

        // Act
        val result = subscriptionRepository.findByUserId(1L, PageRequest.of(0, 10))

        // Assert
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].userId).isEqualTo(1L)
        assertThat(result.content[0].serviceName).isEqualTo("Test Service")
    }

    @Test
    @DisplayName("findByStatus — фильтрация по статусу")
    fun `findByStatus should return subscriptions with given status`() {
        // Arrange
        subscriptionRepository.save(
            Subscription(
                userId = 1L,
                serviceName = "Test Service",
                status = SubscriptionStatus.ACTIVE,
                startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
                endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
                cost = BigDecimal("9.99"),
                autoRenew = true
            )
        )
        subscriptionRepository.save(
            Subscription(
                userId = 1L,
                serviceName = "Cancelled Service",
                status = SubscriptionStatus.CANCELLED,
                startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
                endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
                cost = BigDecimal("5.99"),
                autoRenew = false
            )
        )

        // Act
        val result = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE, PageRequest.of(0, 10))

        // Assert
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].status).isEqualTo(SubscriptionStatus.ACTIVE)
    }

    @Test
    @DisplayName("findExpiredSubscriptions — поиск истёкших")
    fun `findExpiredSubscriptions should return expired active subscriptions`() {
        // Arrange
        subscriptionRepository.save(
            Subscription(
                userId = 1L,
                serviceName = "Expired Service",
                status = SubscriptionStatus.ACTIVE,
                startDate = LocalDateTime.now().minusDays(10),
                endDate = LocalDateTime.now().minusDays(1),
                cost = BigDecimal("9.99"),
                autoRenew = true
            )
        )
        subscriptionRepository.save(
            Subscription(
                userId = 1L,
                serviceName = "Active Service",
                status = SubscriptionStatus.ACTIVE,
                startDate = LocalDateTime.now(),
                endDate = LocalDateTime.now().plusDays(30),
                cost = BigDecimal("5.99"),
                autoRenew = false
            )
        )
        subscriptionRepository.save(
            Subscription(
                userId = 1L,
                serviceName = "Cancelled Expired",
                status = SubscriptionStatus.CANCELLED,
                startDate = LocalDateTime.now().minusDays(10),
                endDate = LocalDateTime.now().minusDays(1),
                cost = BigDecimal("3.99"),
                autoRenew = false
            )
        )

        // Act
        val result = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now())

        // Assert
        assertThat(result).hasSize(1)
        assertThat(result[0].serviceName).isEqualTo("Expired Service")
        assertThat(result[0].endDate).isBefore(LocalDateTime.now())
        assertThat(result[0].status).isEqualTo(SubscriptionStatus.ACTIVE)
    }

    @Test
    @DisplayName("findByServiceName — поиск по названию сервиса")
    fun `findByServiceName should return subscriptions by name`() {
        // Arrange
        subscriptionRepository.save(
            Subscription(
                userId = 1L,
                serviceName = "Netflix",
                status = SubscriptionStatus.ACTIVE,
                startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
                endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
                cost = BigDecimal("9.99"),
                autoRenew = true
            )
        )
        subscriptionRepository.save(
            Subscription(
                userId = 1L,
                serviceName = "YouTube",
                status = SubscriptionStatus.ACTIVE,
                startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
                endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
                cost = BigDecimal("5.99"),
                autoRenew = false
            )
        )

        // Act
        val result = subscriptionRepository.findByServiceName("Netflix", PageRequest.of(0, 10))

        // Assert
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].serviceName).isEqualTo("Netflix")
    }

    @Test
    @DisplayName("findByUserIdAndStatus — фильтрация по пользователю и статусу")
    fun `findByUserIdAndStatus should return subscriptions by user and status`() {
        // Arrange
        subscriptionRepository.save(
            Subscription(
                userId = 1L,
                serviceName = "Active Service",
                status = SubscriptionStatus.ACTIVE,
                startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
                endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
                cost = BigDecimal("9.99"),
                autoRenew = true
            )
        )
        subscriptionRepository.save(
            Subscription(
                userId = 1L,
                serviceName = "Suspended Service",
                status = SubscriptionStatus.SUSPENDED,
                startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
                endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
                cost = BigDecimal("5.99"),
                autoRenew = false
            )
        )
        subscriptionRepository.save(
            Subscription(
                userId = 2L,
                serviceName = "Other User Active",
                status = SubscriptionStatus.ACTIVE,
                startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
                endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
                cost = BigDecimal("3.99"),
                autoRenew = false
            )
        )

        // Act
        val result = subscriptionRepository.findByUserIdAndStatus(
            1L,
            SubscriptionStatus.ACTIVE,
            PageRequest.of(0, 10)
        )

        // Assert
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].userId).isEqualTo(1L)
        assertThat(result.content[0].status).isEqualTo(SubscriptionStatus.ACTIVE)
    }
}