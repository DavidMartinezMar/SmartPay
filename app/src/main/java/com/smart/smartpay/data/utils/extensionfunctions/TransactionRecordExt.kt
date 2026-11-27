package com.smart.smartpay.data.utils.extensionfunctions

import com.smart.smartpay.R
import com.smart.smartpay.data.storage.model.TransactionRecord
import com.smart.smartpay.data.storage.model.TransactionType
import com.smart.smartpay.presentation.utils.formatAmount

fun TransactionRecord.typeLabel(): Int = when (type) {
    TransactionType.SALE -> R.string.history_type_sale
    TransactionType.CANCELLATION -> R.string.history_type_cancellation
    TransactionType.REFUND -> R.string.history_type_refund
}

/** Sales positive; cancellations and refunds give money back. */
fun TransactionRecord.signedAmount(): String =
    if (type == TransactionType.SALE) formatAmount(totalInCents) else "-${formatAmount(totalInCents)}"

/** Status of a sale after operations on it, if any. */
fun TransactionRecord.statusLabel(): Int? = when {
    type != TransactionType.SALE -> null
    isCancelled -> R.string.history_status_cancelled
    refundedInCents >= totalInCents -> R.string.history_status_refunded
    refundedInCents > 0 -> R.string.history_status_partially_refunded
    else -> null
}