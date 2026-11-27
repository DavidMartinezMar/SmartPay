package com.smart.smartpay.data.storage.model

import java.time.Instant
import java.time.ZoneId

/**
 * A transaction of the history. For a sale, [operations] are the cancellation / refunds made on it,
 * and the rules below say what can still be done with it.
 */
data class TransactionRecord(
    val id: Long,
    val type: TransactionType,
    val amountInCents: Long,
    val tipInCents: Long,
    /** SDK SaleCurrency name. */
    val currency: String,
    val authorizationCode: String,
    val reference: String,
    val uniqueReference: String,
    val transToken: String?,
    val maskedPan: String,
    val cardBrand: String,
    val createdAt: Long,
    val originalId: Long?,
    val operations: List<TransactionRecord> = emptyList(),
) {
    val totalInCents: Long get() = amountInCents + tipInCents

    val isCancelled: Boolean get() = operations.any { it.type == TransactionType.CANCELLATION }

    val refundedInCents: Long get() = operations.filter { it.type == TransactionType.REFUND }.sumOf { it.amountInCents }

    /** What can still be refunded: the sale total minus previous refunds (0 if cancelled). */
    val refundableInCents: Long
        get() = if (type == TransactionType.SALE && !isCancelled) (totalInCents - refundedInCents).coerceAtLeast(0) else 0

    val canRefund: Boolean
        get() = refundableInCents > 0 && authorizationCode.isNotBlank() && uniqueReference.isNotBlank()

    /**
     * A sale can be cancelled only the day it was made (before the batch closes) and if it had no
     * refunds; the host has the last word.
     */
    fun canCancel(now: Long, zone: ZoneId = ZoneId.systemDefault()): Boolean =
        type == TransactionType.SALE &&
            !isCancelled &&
            refundedInCents == 0L &&
            reference.isNotBlank() &&
            uniqueReference.isNotBlank() &&
            Instant.ofEpochMilli(createdAt).atZone(zone).toLocalDate() == Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
}
