package com.example.subscriptionservice.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime

data class SubscriptionRequestDto(
    @field:NotNull(message = "ID пользователя не может быть пустым")
    val userId: Long,

    @field:NotBlank(message = "Название сервиса не может быть пустым")
    @field:Size(max = 255, message = "Название сервиса не должно превышать 255 символов")
    val serviceName: String,

    @field:NotNull(message = "Дата начала не может быть пустой")
    val startDate: LocalDateTime,

    @field:NotNull(message = "Дата окончания не может быть пустой")
    val endDate: LocalDateTime,

    @field:DecimalMin(value = "0.0", inclusive = false, message = "Стоимость должна быть больше нуля")
    @field:Digits(integer = 10, fraction = 2, message = "Стоимость имеет неверный формат")
    val cost: BigDecimal,

    val autoRenew: Boolean = false
) {
    @AssertTrue(message = "Дата окончания должна быть позже даты начала")
    fun isEndDateAfterStartDate(): Boolean {
        return endDate.isAfter(startDate)
    }
}