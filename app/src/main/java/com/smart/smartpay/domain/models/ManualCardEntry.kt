package com.smart.smartpay.domain.models

/**
 * Card data for a manual (card not present) sale.
 *
 * @property expirationDate month and year as printed on the card: "MMYY" or "MM/YY".
 */
data class ManualCardData(
    val cardNumber: String,
    val cardHolderName: String,
    val expirationDate: String,
    val cvv: String,
) {
    override fun toString(): String = "ManualCardData(****)" // never log card data
}