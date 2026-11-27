package com.smart.smartpay.presentation.models

/** What the terminal is doing while the sale runs. */
sealed interface PaymentStatus {
    data object Starting : PaymentStatus
    data object WaitingForCard : PaymentStatus
    data object ReadingCard : PaymentStatus
    data class CardDetected(val label: String) : PaymentStatus
    data class Message(val text: String) : PaymentStatus
    data object EnteringPin : PaymentStatus
    data object Processing : PaymentStatus
}