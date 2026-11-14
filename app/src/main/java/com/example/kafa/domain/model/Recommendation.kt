package com.example.kafa.domain.model

data class Recommendation(
    val event: Event,
    val urgencyScore: Float,
    val overdueDays: Long,
    val message: String
)
