package com.smart.smartpay.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation routes. Add one object/data class per new screen or flow. */
@Serializable
data object SplashRoute

@Serializable
data object SaleRoute

/** Tip selection for a sale of [amountInCents] captured in the Sale screen. */
@Serializable
data class TipRoute(val amountInCents: Long)

/** Card payment of [amountInCents] plus [tipInCents]. */
@Serializable
data class PaymentRoute(val amountInCents: Long, val tipInCents: Long)

enum class CardOperationType { CANCELLATION, REFUND }

/**
 * Cancellation or refund of the sale [transactionId] of the history.
 * [amountInCents] is the amount to refund (for a cancellation, the sale total shown on screen).
 */
@Serializable
data class CardOperationRoute(
    val type: CardOperationType,
    val transactionId: Long,
    val amountInCents: Long,
)

/** Approved transactions of this terminal. */
@Serializable
data object HistoryRoute

/** A transaction of the history, with its cancel / refund actions. */
@Serializable
data class TransactionDetailRoute(val transactionId: Long)
