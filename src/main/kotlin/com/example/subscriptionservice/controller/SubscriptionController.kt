package com.example.subscriptionservice.controller

import com.example.subscriptionservice.dto.*
import com.example.subscriptionservice.service.SubscriptionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/subscriptions")
@Validated
@Tag(name = "Subscriptions", description = "API для управления пользовательскими подписками")
open class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {

    @PostMapping
    @Operation(summary = "Создать новую подписку", description = "Создает подписку со статусом ACTIVE")
    open fun createSubscription(
        @RequestBody @Valid request: SubscriptionRequestDto
    ): ResponseEntity<SubscriptionResponseDto> {
        val response = subscriptionService.createSubscription(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить подписку по ID")
    open fun getSubscriptionById(
        @Parameter(description = "ID подписки", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<SubscriptionResponseDto> {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id))
    }

    @GetMapping
    @Operation(summary = "Получить список подписок с фильтрацией и пагинацией")
    open fun getAllSubscriptions(
        @ParameterObject filter: SubscriptionFilterDto,
        @Parameter(description = "Номер страницы (начиная с 0)", example = "0") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Размер страницы", example = "10") @RequestParam(defaultValue = "10") size: Int,
        @Parameter(description = "Поле для сортировки", example = "startDate") @RequestParam(defaultValue = "createdAt") sortBy: String,
        @Parameter(description = "Направление сортировки", example = "ASC") @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<Page<SubscriptionResponseDto>> {
        val direction = if (sortDirection.equals("DESC", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(direction, sortBy))
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions(filter, pageable))
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Изменить статус подписки")
    open fun updateStatus(
        @Parameter(description = "ID подписки") @PathVariable id: Long,
        @RequestBody @Valid updateDto: SubscriptionUpdateStatusDto
    ): ResponseEntity<SubscriptionResponseDto> {
        return ResponseEntity.ok(subscriptionService.updateStatus(id, updateDto))
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Отменить подписку")
    open fun cancelSubscription(
        @Parameter(description = "ID подписки") @PathVariable id: Long
    ): ResponseEntity<SubscriptionResponseDto> {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id))
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Приостановить подписку")
    open fun suspendSubscription(
        @Parameter(description = "ID подписки") @PathVariable id: Long
    ): ResponseEntity<SubscriptionResponseDto> {
        return ResponseEntity.ok(subscriptionService.suspendSubscription(id))
    }

    @GetMapping("/user/{userId}/active")
    @Operation(summary = "Получить активные подписки пользователя")
    open fun getActiveSubscriptionsByUserId(
        @Parameter(description = "ID пользователя") @PathVariable userId: Long
    ): ResponseEntity<List<SubscriptionResponseDto>> {
        return ResponseEntity.ok(subscriptionService.getActiveSubscriptionsByUserId(userId))
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Получить историю изменений статуса подписки")
    open fun getSubscriptionHistory(
        @Parameter(description = "ID подписки") @PathVariable id: Long
    ): ResponseEntity<List<SubscriptionHistoryResponseDto>> {
        return ResponseEntity.ok(subscriptionService.getSubscriptionHistory(id))
    }
}