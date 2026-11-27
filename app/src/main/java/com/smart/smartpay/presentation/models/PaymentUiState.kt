package com.smart.smartpay.presentation.models

import com.smart.smartpay.domain.payment.SaleEvent

data class PaymentUiState(
    val amountInCents: Long,
    val tipInCents: Long,
    val status: PaymentStatus = PaymentStatus.Starting,
    /** Question waiting for the customer (a SaleEvent carrying a Decision). */
    val prompt: SaleEvent? = null,
    val pinDigits: Int = 0,
    val pinMessage: String? = null,
    val result: PaymentResult? = null,
    /** History id of the approved sale, once recorded (needed to cancel / refund it). */
    val transactionId: Long? = null,
) {
    val totalInCents: Long get() = amountInCents + tipInCents
    val canCancel: Boolean get() = result == null && status == PaymentStatus.WaitingForCard
}