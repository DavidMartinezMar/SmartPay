package com.smart.smartpay.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Max digits accepted (in cents): 9,999,999.99 */
private const val MAX_AMOUNT_DIGITS = 9

private const val KEY_DOUBLE_ZERO = "00"
private const val KEY_DELETE = "⌫"

private val keypadRows = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf(KEY_DOUBLE_ZERO, "0", KEY_DELETE),
)

/**
 * Numeric keypad to capture an amount POS-style, in cents: each digit enters from the right
 * (typing 1, 2, 5 gives "125" = $1.25). Keep the digits with [applyAmountKey].
 */
@Composable
fun AmountKeypad(
    onKeyPressed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        keypadRows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    FilledTonalButton(
                        onClick = { onKeyPressed(key) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.6f),
                    ) {
                        Text(text = key, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

/** Returns the new digit string after pressing [key]; ignores input beyond the limit and leading zeros. */
fun applyAmountKey(current: String, key: String): String = when (key) {
    KEY_DELETE -> current.dropLast(1)
    else -> (current + key).trimStart('0').let { next ->
        if (next.length > MAX_AMOUNT_DIGITS) current else next
    }
}

/** Amount in cents represented by the keypad [digits] ("" = 0). */
fun amountFromDigits(digits: String): Long = digits.toLongOrNull() ?: 0L
