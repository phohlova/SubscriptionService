package com.example.subscriptionservice.task

import com.example.subscriptionservice.service.SubscriptionService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledTasks(
    private val subscriptionService: SubscriptionService
) {

    private val log = LoggerFactory.getLogger(ScheduledTasks::class.java)

    @Scheduled(fixedRate = 60000)
    fun checkExpiredSubscriptions() {
        log.info(" Запуск задачи проверки истекших подписок...")

        try {
            val count = subscriptionService.updateExpiredSubscriptions()

            if (count > 0) {
                log.info(" Обновлено статусов на EXPIRED: $count")
            } else {
                log.debug(" Истекших подписок не найдено.")
            }
        } catch (e: Exception) {
            log.error(" Ошибка при проверке истекших подписок", e)
        }
    }
}