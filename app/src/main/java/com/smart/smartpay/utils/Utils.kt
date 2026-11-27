package com.smart.smartpay.utils

import java.text.DateFormat
import java.util.Date

/** Returns the new digit string after pressing [key]; ignores input beyond the limit and leading zeros. */
fun applyAmountKey(current: String, key: String): String = when (key) {
    KEY_DELETE -> current.dropLast(1)
    else -> (current + key).trimStart('0').let { next ->
        if (next.length > MAX_AMOUNT_DIGITS) current else next
    }
}

/** Amount in cents represented by the keypad [digits] ("" = 0). */
fun amountFromDigits(digits: String): Long = digits.toLongOrNull() ?: 0L

fun formatDateTime(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))