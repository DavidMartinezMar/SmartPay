package com.smart.smartpay.domain.payment

/** Data of a sale. Amounts are in cents (e.g. 12550 = $125.50). */
data class SaleRequest(
    val amountInCents: Long,
    val tipInCents: Long = 0,
    val currency: SaleCurrency = SaleCurrency.MXN,
    /** Merchant reference. If null, one is generated. */
    val reference: String? = null,
    val cashierName: String? = null,
    /** Card typed in instead of presented (only if the merchant allows manual entry). */
    val manualCard: ManualCardData? = null,
) {
    init {
        require(amountInCents > 0) { "amountInCents must be greater than 0" }
        require(tipInCents >= 0) { "tipInCents can't be negative" }
    }
}

enum class SaleCurrency(internal val code: String) {
    MXN("484"),
    USD("840"),
}

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
