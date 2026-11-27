package com.smart.smartpay.utils

const val ENABLED_USD = 1
/** core-payments' EMV transaction type for a sale */
const val TRANSACTION_TYPE_SALE = "1"
const val TRANS_TOKEN = "TRANS_TOKEN"
const val STOP_TIMEOUT_MS = 5_000L
/** Max digits accepted (in cents): 9,999,999.99 */
const val MAX_AMOUNT_DIGITS = 9
const val KEY_DOUBLE_ZERO = "00"
const val KEY_DELETE = "⌫"
val keypadRows = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf(KEY_DOUBLE_ZERO, "0", KEY_DELETE),
)
const val MAX_DIGITS = 9
