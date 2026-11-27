package com.smart.smartpay.domain.models

enum class CardDigitsType {
    /** Last 4 digits of the card number (swiped card validation). */
    LAST_FOUR_DIGITS,
    /** Security code (American Express swiped card). */
    CVV,
}