package com.smart.smartpay.data.utils.extensionfunctions

import com.smart.smartpay.data.storage.local.TransactionEntity
import com.smart.smartpay.data.storage.model.TransactionRecord
import com.smart.smartpay.data.storage.model.TransactionType

/** Entities to records, attaching to each sale the operations made on it. */
fun List<TransactionEntity>.toRecords(): List<TransactionRecord> {
    val records = map { it.toRecord() }
    val operationsBySale = records.filter { it.originalId != null }.groupBy { it.originalId }
    return records.map { record ->
        if (record.type == TransactionType.SALE) record.copy(operations = operationsBySale[record.id].orEmpty()) else record
    }
}

fun TransactionEntity.toRecord() = TransactionRecord(
    id = id,
    type = TransactionType.entries.firstOrNull { it.name == type } ?: TransactionType.SALE,
    amountInCents = amountInCents,
    tipInCents = tipInCents,
    currency = currency,
    authorizationCode = authorizationCode,
    reference = reference,
    uniqueReference = uniqueReference,
    transToken = transToken,
    maskedPan = maskedPan,
    cardBrand = cardBrand,
    createdAt = createdAt,
    originalId = originalId,
)