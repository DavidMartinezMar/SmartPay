package com.smart.smartpay.domain.models

/**
 * Data of a processed card transaction (sale, cancellation or refund), as returned by the host.
 *
 * Keep the receipt of an approved sale to cancel or refund it later:
 * [com.smart.smartpay.domain.operation.CancellationRequest.of] and
 * [com.smart.smartpay.domain.operation.RefundRequest.of].
 */
data class SaleReceipt(
    val authorizationCode: String,
    /** Host reference of the transaction. */
    val reference: String,
    /** Unique reference the terminal sent with the transaction. */
    val uniqueReference: String,
    val maskedPan: String,
    val cardHolderName: String,
    val cardBrand: String,
    val cardType: String,
    val bankName: String,
    val applicationLabel: String,
    val aid: String,
    val arqc: String,
    val entryMode: String,
    val date: String,
    val time: String,
    val folio: String,
    val merchantId: String,
    val terminalId: String,
    val signatureRequired: Boolean,
    val headers: List<String>,
    val footers: List<String>,
    /** Every field returned by the host, for anything not mapped above. */
    val raw: Map<String, String>,
)
