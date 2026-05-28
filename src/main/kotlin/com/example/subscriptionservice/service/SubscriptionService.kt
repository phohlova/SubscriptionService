package com.example.subscriptionservice.service

import com.example.subscriptionservice.domain.entity.Subscription
import com.example.subscriptionservice.domain.SubscriptionStatus
import com.example.subscriptionservice.domain.entity.SubscriptionHistory
import com.example.subscriptionservice.dto.SubscriptionFilterDto
import com.example.subscriptionservice.dto.SubscriptionHistoryResponseDto
import com.example.subscriptionservice.dto.SubscriptionRequestDto
import com.example.subscriptionservice.dto.SubscriptionResponseDto
import com.example.subscriptionservice.dto.SubscriptionUpdateStatusDto
import com.example.subscriptionservice.repository.SubscriptionHistoryRepository
import com.example.subscriptionservice.repository.SubscriptionRepository
import com.example.subscriptionservice.specification.SubscriptionSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
open class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val historyRepository: SubscriptionHistoryRepository
) {

    /**
     * Создание новой подписки
     */
    open fun createSubscription(dto: SubscriptionRequestDto): SubscriptionResponseDto {
        val subscription = Subscription(
            userId = dto.userId,
            serviceName = dto.serviceName,
            startDate = dto.startDate,
            endDate = dto.endDate,
            cost = dto.cost,
            autoRenew = dto.autoRenew,
            status = SubscriptionStatus.ACTIVE
        )

        val saved = subscriptionRepository.save(subscription)
        return mapToResponse(saved)
    }

    /**
     * Получение подписки по ID
     */
    @Transactional(readOnly = true)
    open fun getSubscriptionById(id: Long): SubscriptionResponseDto {
        val subscription = subscriptionRepository.findById(id)
            .orElseThrow { RuntimeException("Подписка с ID $id не найдена") }
        return mapToResponse(subscription)
    }

    /**
     * Получение всех подписок с фильтрами и пагинацией
     */
    @Transactional(readOnly = true)
    open fun getAllSubscriptions(
        filter: SubscriptionFilterDto,
        pageable: Pageable
    ): Page<SubscriptionResponseDto> {
        val specification = SubscriptionSpecification.toSpecification(filter)
        return subscriptionRepository.findAll(specification, pageable)
            .map { subscription -> mapToResponse(subscription) }
    }

    /**
     * Получение активных подписок пользователя
     */
    @Transactional(readOnly = true)
    open fun getActiveSubscriptionsByUserId(userId: Long): List<SubscriptionResponseDto> {
        return subscriptionRepository.findActiveSubscriptionsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .map { subscription -> mapToResponse(subscription) }
    }

    /**
     * Обновление статуса подписки
     */
    open fun updateStatus(id: Long, dto: SubscriptionUpdateStatusDto): SubscriptionResponseDto {
        val subscription = subscriptionRepository.findById(id)
            .orElseThrow { RuntimeException("Подписка с ID $id не найдена") }

        val oldStatus = subscription.status

        // Валидация: нельзя активировать истёкшую подписку без продления
        if (dto.status == SubscriptionStatus.ACTIVE &&
            oldStatus == SubscriptionStatus.EXPIRED &&
            subscription.endDate.isBefore(LocalDateTime.now())) {
            throw RuntimeException("Нельзя активировать истёкшую подписку без продления срока действия")
        }

        subscription.status = dto.status
        val updated = subscriptionRepository.save(subscription)

        val historyRecord = SubscriptionHistory(
            subscription = updated,
            oldStatus = oldStatus,
            newStatus = dto.status
        )
        historyRepository.save(historyRecord)

        return mapToResponse(updated)
    }

    /**
     * Получение истории изменений подписки
     */
    @Transactional(readOnly = true)
    open fun getSubscriptionHistory(id: Long): List<SubscriptionHistoryResponseDto> {
        // Проверяем, существует ли подписка
        if (!subscriptionRepository.existsById(id)) {
            throw RuntimeException("Подписка с ID $id не найдена")
        }
        return historyRepository.findBySubscriptionIdOrderByChangedAtDesc(id)
            .map {
                SubscriptionHistoryResponseDto(
                    id = it.id!!,
                    subscriptionId = it.subscription?.id!!,
                    oldStatus = it.oldStatus,
                    newStatus = it.newStatus,
                    changedAt = it.changedAt
                )
            }
    }

    /**
     * Отмена подписки
     */
    open fun cancelSubscription(id: Long): SubscriptionResponseDto {
        val subscription = subscriptionRepository.findById(id)
            .orElseThrow { RuntimeException("Подписка с ID $id не найдена") }

        subscription.status = SubscriptionStatus.CANCELLED
        val updated = subscriptionRepository.save(subscription)
        return mapToResponse(updated)
    }

    /**
     * Приостановка подписки
     */
    open fun suspendSubscription(id: Long): SubscriptionResponseDto {
        val subscription = subscriptionRepository.findById(id)
            .orElseThrow { RuntimeException("Подписка с ID $id не найдена") }

        subscription.status = SubscriptionStatus.SUSPENDED
        val updated = subscriptionRepository.save(subscription)
        return mapToResponse(updated)
    }

    /**
     * Обновление истёкших подписок (для scheduler)
     */
    open fun updateExpiredSubscriptions(): Int {
        val expired = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now())
        expired.forEach { subscription -> subscription.status = SubscriptionStatus.EXPIRED }
        return if (expired.isNotEmpty()) {
            subscriptionRepository.saveAll(expired).count().toInt()
        } else 0
    }

    /**
     * Маппинг сущности в DTO ответа
     */
    private fun mapToResponse(subscription: Subscription): SubscriptionResponseDto {
        return SubscriptionResponseDto(
            id = subscription.id!!,
            userId = subscription.userId,
            serviceName = subscription.serviceName,
            status = subscription.status,
            startDate = subscription.startDate,
            endDate = subscription.endDate,
            cost = subscription.cost,
            autoRenew = subscription.autoRenew,
            createdAt = subscription.createdAt!!,
            updatedAt = subscription.updatedAt
        )
    }
}