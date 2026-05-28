package com.example.subscriptionservice.service

import com.example.subscriptionservice.domain.entity.Subscription
import com.example.subscriptionservice.domain.entity.SubscriptionHistory
import com.example.subscriptionservice.domain.SubscriptionStatus
import com.example.subscriptionservice.dto.*
import com.example.subscriptionservice.repository.SubscriptionHistoryRepository
import com.example.subscriptionservice.repository.SubscriptionRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("SubscriptionService — Unit тесты")
class SubscriptionServiceTest {

    @Mock
    private lateinit var subscriptionRepository: SubscriptionRepository

    @Mock
    private lateinit var historyRepository: SubscriptionHistoryRepository

    private lateinit var subscriptionService: SubscriptionService

    private lateinit var testRequest: SubscriptionRequestDto
    private lateinit var testSubscription: Subscription

    @BeforeEach
    fun setUp() {
        subscriptionService = SubscriptionService(subscriptionRepository, historyRepository)

        testRequest = SubscriptionRequestDto(
            userId = 1L,
            serviceName = "Test Service",
            startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
            endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
            cost = BigDecimal("9.99"),
            autoRenew = true
        )

        testSubscription = Subscription(
            id = 1L,
            userId = 1L,
            serviceName = "Test Service",
            status = SubscriptionStatus.ACTIVE,
            startDate = testRequest.startDate,
            endDate = testRequest.endDate,
            cost = testRequest.cost,
            autoRenew = testRequest.autoRenew,
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    @DisplayName("createSubscription: успешное создание")
    fun `createSubscription should create and return response`() {
        Mockito.`when`(subscriptionRepository.save(any()))
            .thenReturn(testSubscription)

        val result = subscriptionService.createSubscription(testRequest)

        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(1L)
        assertThat(result.serviceName).isEqualTo("Test Service")
        assertThat(result.status).isEqualTo(SubscriptionStatus.ACTIVE)
    }

    @Test
    @DisplayName("getSubscriptionById: подписка найдена")
    fun `getSubscriptionById should return response when found`() {
        Mockito.`when`(subscriptionRepository.findById(Mockito.eq(1L)))
            .thenReturn(Optional.of(testSubscription))

        val result = subscriptionService.getSubscriptionById(1L)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.serviceName).isEqualTo("Test Service")
    }

    @Test
    @DisplayName("getSubscriptionById: подписка не найдена")
    fun `getSubscriptionById should throw exception when not found`() {
        Mockito.`when`(subscriptionRepository.findById(1L))
            .thenReturn(Optional.empty())

        assertThatThrownBy { subscriptionService.getSubscriptionById(1L) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Подписка с ID 1 не найдена")
    }

    @Test
    @DisplayName("updateStatus: успешное изменение статуса + запись в историю")
    fun `updateStatus should update status and save history`() {
        val updateDto = SubscriptionUpdateStatusDto(SubscriptionStatus.SUSPENDED)
        testSubscription.status = SubscriptionStatus.ACTIVE

        Mockito.`when`(subscriptionRepository.findById(1L))
            .thenReturn(Optional.of(testSubscription))
        Mockito.`when`(subscriptionRepository.save(any()))
            .thenReturn(testSubscription)

        val result = subscriptionService.updateStatus(1L, updateDto)

        assertThat(result.status).isEqualTo(SubscriptionStatus.SUSPENDED)
    }

    @Test
    @DisplayName("updateStatus: нельзя активировать истёкшую подписку")
    fun `updateStatus should throw exception when activating expired subscription`() {
        testSubscription.status = SubscriptionStatus.EXPIRED
        testSubscription.endDate = LocalDateTime.now().minusDays(1)
        val updateDto = SubscriptionUpdateStatusDto(SubscriptionStatus.ACTIVE)

        Mockito.`when`(subscriptionRepository.findById(1L))
            .thenReturn(Optional.of(testSubscription))

        assertThatThrownBy { subscriptionService.updateStatus(1L, updateDto) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Нельзя активировать истёкшую подписку без продления срока действия")
    }

    @Test
    @DisplayName("getSubscriptionHistory: возврат истории")
    fun `getSubscriptionHistory should return history list`() {
        val history = listOf(
            SubscriptionHistory(
                id = 1L,
                subscription = testSubscription,
                oldStatus = SubscriptionStatus.ACTIVE,
                newStatus = SubscriptionStatus.SUSPENDED,
                changedAt = LocalDateTime.now()
            )
        )
        Mockito.`when`(subscriptionRepository.existsById(1L)).thenReturn(true)
        Mockito.`when`(historyRepository.findBySubscriptionIdOrderByChangedAtDesc(1L))
            .thenReturn(history)

        val result = subscriptionService.getSubscriptionHistory(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].newStatus).isEqualTo(SubscriptionStatus.SUSPENDED)
    }

    @Test
    @DisplayName("getActiveSubscriptionsByUserId: активные подписки пользователя")
    fun `getActiveSubscriptionsByUserId should return active subscriptions`() {
        val activeList = listOf(testSubscription)
        Mockito.`when`(
            subscriptionRepository.findActiveSubscriptionsByUserIdAndStatus(
                1L,
                SubscriptionStatus.ACTIVE
            )
        ).thenReturn(activeList)

        val result = subscriptionService.getActiveSubscriptionsByUserId(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].status).isEqualTo(SubscriptionStatus.ACTIVE)
    }

    @Test
    @DisplayName("cancelSubscription: отмена подписки")
    fun `cancelSubscription should cancel subscription`() {
        testSubscription.status = SubscriptionStatus.ACTIVE
        Mockito.`when`(subscriptionRepository.findById(1L))
            .thenReturn(Optional.of(testSubscription))
        Mockito.`when`(subscriptionRepository.save(any()))
            .thenReturn(testSubscription)

        val result = subscriptionService.cancelSubscription(1L)

        assertThat(result.status).isEqualTo(SubscriptionStatus.CANCELLED)
    }

    @Test
    @DisplayName("suspendSubscription: приостановка подписки")
    fun `suspendSubscription should suspend subscription`() {
        testSubscription.status = SubscriptionStatus.ACTIVE
        Mockito.`when`(subscriptionRepository.findById(1L))
            .thenReturn(Optional.of(testSubscription))
        Mockito.`when`(subscriptionRepository.save(any()))
            .thenReturn(testSubscription)

        val result = subscriptionService.suspendSubscription(1L)

        assertThat(result.status).isEqualTo(SubscriptionStatus.SUSPENDED)
    }
}