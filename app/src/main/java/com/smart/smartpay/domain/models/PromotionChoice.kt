package com.smart.smartpay.domain.models

sealed interface PromotionChoice {
    data object None : PromotionChoice
    data class MonthsWithoutInterest(val months: Int) : PromotionChoice
    data class MonthsWithInterest(val months: Int) : PromotionChoice
    data object LoyaltyPayment : PromotionChoice
}