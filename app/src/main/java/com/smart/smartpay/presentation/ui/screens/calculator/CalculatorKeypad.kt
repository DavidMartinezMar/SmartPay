package com.smart.smartpay.presentation.ui.screens.calculator

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
import com.smart.smartpay.utils.keypadRows

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
