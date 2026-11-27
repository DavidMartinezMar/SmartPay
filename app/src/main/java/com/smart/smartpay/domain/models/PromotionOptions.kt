package com.smart.smartpay.domain.models

/** Promotions the issuer offers for this card and amount. */
data class PromotionOptions(
    val monthsWithoutInterest: List<Int>,
    val monthsWithInterest: List<Int>,
    val loyaltyPaymentAvailable: Boolean,
)