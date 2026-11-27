package com.smart.smartpay.domain.utils.extensionfunctions

import com.smart.smartpay.data.storage.local.TransactionEntity
import com.smart.smartpay.data.storage.model.TransactionType
import com.smart.smartpay.data.utils.currencyOf
import com.smart.smartpay.domain.models.SaleReceipt
import com.smart.smartpay.utils.TRANS_TOKEN

fun SaleReceipt.toEntity(type: TransactionType, amountInCents: Long, tipInCents: Long, originalId: Long?, now: () -> Long) =
    TransactionEntity(
        type = type.name,
        amountInCents = amountInCents,
        tipInCents = tipInCents,
        currency = currencyOf(this).name,
        authorizationCode = authorizationCode,
        reference = reference,
        uniqueReference = uniqueReference,
        transToken = raw[TRANS_TOKEN]?.takeIf { it.isNotBlank() },
        maskedPan = maskedPan,
        cardBrand = cardBrand,
        createdAt = now(),
        originalId = originalId,
    )