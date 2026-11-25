package com.smart.smartpay.domain.payment

/** An application of a multi-application card (e.g. debit / credit). */
data class CardApplication(val id: String, val name: String)

enum class CardDigitsType {
    /** Last 4 digits of the card number (swiped card validation). */
    LAST_FOUR_DIGITS,

    /** Security code (American Express swiped card). */
    CVV,
}

/** Promotions the issuer offers for this card and amount. */
data class PromotionOptions(
    val monthsWithoutInterest: List<Int>,
    val monthsWithInterest: List<Int>,
    val loyaltyPaymentAvailable: Boolean,
)

sealed interface PromotionChoice {
    data object None : PromotionChoice
    data class MonthsWithoutInterest(val months: Int) : PromotionChoice
    data class MonthsWithInterest(val months: Int) : PromotionChoice
    data object LoyaltyPayment : PromotionChoice
}

/** Dynamic Currency Conversion offer: pay a foreign card in its own currency. */
data class DccOffer(
    val localAmount: String,
    val exchangeRate: String,
    val mxnVsCurrency: String,
    val markUp: String,
    val foreignAmount: String,
    val foreignCurrency: String,
    val reference: String,
)

enum class DccChoice {
    /** Pay in the card's currency ([DccOffer.foreignAmount]). */
    FOREIGN_CURRENCY,

    /** Pay in pesos. */
    LOCAL_CURRENCY,
}

data class Intercard(
    val uid: String,
    val sak: String,
    val atqa: String,
    val siteCode: String,
    val technology: String,
    val accountNumber: String,
    val encryptedAccountNumber: String
)
