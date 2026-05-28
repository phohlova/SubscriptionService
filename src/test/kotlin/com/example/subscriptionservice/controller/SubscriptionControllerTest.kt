package com.example.subscriptionservice.controller

import com.example.subscriptionservice.domain.SubscriptionStatus
import com.example.subscriptionservice.dto.*
import com.example.subscriptionservice.service.SubscriptionService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDateTime

@WebMvcTest(SubscriptionController::class)
@DisplayName("SubscriptionController — Интеграционные тесты")
class SubscriptionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var subscriptionService: SubscriptionService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var testResponse: SubscriptionResponseDto
    private lateinit var testRequest: SubscriptionRequestDto

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().apply {
            registerModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        testRequest = SubscriptionRequestDto(
            userId = 1L,
            serviceName = "Test Service",
            startDate = LocalDateTime.of(2024, 1, 1, 10, 0),
            endDate = LocalDateTime.of(2025, 1, 1, 10, 0),
            cost = BigDecimal("9.99"),
            autoRenew = true
        )

        testResponse = SubscriptionResponseDto(
            id = 1L,
            userId = 1L,
            serviceName = "Test Service",
            status = SubscriptionStatus.ACTIVE,
            startDate = testRequest.startDate,
            endDate = testRequest.endDate,
            cost = testRequest.cost,
            autoRenew = testRequest.autoRenew,
            createdAt = LocalDateTime.now(),
            updatedAt = null
        )
    }

    @Test
    @DisplayName("POST /api/subscriptions — создание подписки")
    fun `createSubscription should return 201 Created`() {
        Mockito.`when`(subscriptionService.createSubscription(testRequest))
            .thenReturn(testResponse)

        mockMvc.perform(post("/api/subscriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.serviceName").value("Test Service"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
    }

    @Test
    @DisplayName("POST /api/subscriptions — валидация: endDate <= startDate")
    fun `createSubscription should return 400 when endDate before startDate`() {
        val invalidRequest = testRequest.copy(
            startDate = LocalDateTime.of(2025, 1, 1, 10, 0),
            endDate = LocalDateTime.of(2024, 1, 1, 10, 0)
        )

        mockMvc.perform(post("/api/subscriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("GET /api/subscriptions/{id} — получение по ID")
    fun `getSubscriptionById should return 200 OK`() {
        Mockito.`when`(subscriptionService.getSubscriptionById(1L))
            .thenReturn(testResponse)

        mockMvc.perform(get("/api/subscriptions/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.serviceName").value("Test Service"))
    }

    @Test
    @DisplayName("PATCH /api/subscriptions/{id}/status — изменение статуса")
    fun `updateStatus should return 200 OK`() {
        val updateDto = SubscriptionUpdateStatusDto(SubscriptionStatus.SUSPENDED)
        val updatedResponse = testResponse.copy(status = SubscriptionStatus.SUSPENDED)

        Mockito.`when`(subscriptionService.updateStatus(1L, updateDto))
            .thenReturn(updatedResponse)

        mockMvc.perform(patch("/api/subscriptions/1/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUSPENDED"))
    }

    @Test
    @DisplayName("GET /api/subscriptions/{id}/history — получение истории")
    fun `getSubscriptionHistory should return 200 OK with list`() {
        val historyResponse = listOf(
            SubscriptionHistoryResponseDto(
                id = 1L,
                subscriptionId = 1L,
                oldStatus = SubscriptionStatus.ACTIVE,
                newStatus = SubscriptionStatus.SUSPENDED,
                changedAt = LocalDateTime.now()
            )
        )

        Mockito.`when`(subscriptionService.getSubscriptionHistory(1L))
            .thenReturn(historyResponse)

        mockMvc.perform(get("/api/subscriptions/1/history"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].newStatus").value("SUSPENDED"))
    }

    @Test
    @DisplayName("GET /api/subscriptions/user/{userId}/active — активные подписки пользователя")
    fun `getActiveSubscriptionsByUserId should return list`() {
        val activeList = listOf(testResponse)

        Mockito.`when`(subscriptionService.getActiveSubscriptionsByUserId(1L))
            .thenReturn(activeList)

        mockMvc.perform(get("/api/subscriptions/user/1/active"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].status").value("ACTIVE"))
    }

    @Test
    @DisplayName("DELETE /api/subscriptions/{id}/cancel — отмена подписки")
    fun `cancelSubscription should return 200 OK`() {
        val cancelledResponse = testResponse.copy(status = SubscriptionStatus.CANCELLED)

        Mockito.`when`(subscriptionService.cancelSubscription(1L))
            .thenReturn(cancelledResponse)

        mockMvc.perform(delete("/api/subscriptions/1/cancel"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CANCELLED"))
    }

    @Test
    @DisplayName("PATCH /api/subscriptions/{id}/suspend — приостановка подписки")
    fun `suspendSubscription should return 200 OK`() {
        val suspendedResponse = testResponse.copy(status = SubscriptionStatus.SUSPENDED)

        Mockito.`when`(subscriptionService.suspendSubscription(1L))
            .thenReturn(suspendedResponse)

        mockMvc.perform(patch("/api/subscriptions/1/suspend"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUSPENDED"))
    }
}