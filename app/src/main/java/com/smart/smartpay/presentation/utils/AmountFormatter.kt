package com.smart.smartpay.presentation.utils

import java.text.NumberFormat

/** Formats an amount in cents with the device locale currency (e.g. 12550 -> "$125.50"). */
fun formatAmount(amountInCents: Long): String =
    NumberFormat.getCurrencyInstance().format(amountInCents / 100.0)
