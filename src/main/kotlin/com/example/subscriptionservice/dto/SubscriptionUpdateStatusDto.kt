package com.example.subscriptionservice.dto

import com.example.subscriptionservice.domain.SubscriptionStatus
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionUpdateStatusDto @JsonCreator constructor(
    @JsonProperty("status") val status: SubscriptionStatus
)