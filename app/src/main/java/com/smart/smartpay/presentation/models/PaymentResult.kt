package com.smart.smartpay.presentation.models

import com.smart.smartpay.domain.models.Intercard
import com.smart.smartpay.domain.models.SaleReceipt

sealed interface PaymentResult {
    data class Approved(val receipt: SaleReceipt) : PaymentResult

    /** [retryWithChip]: contactless failed, retry inserting the card. */
    data class Declined(val message: String, val code: String, val retryWithChip: Boolean) : PaymentResult

    data class Failed(val message: String, val code: String) : PaymentResult

    data class IntercardCard(val card: Intercard) : PaymentResult
}