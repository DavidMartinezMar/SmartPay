package com.smart.smartpay.features.tip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.smartpay.R
import com.smart.smartpay.ui.components.AmountKeypad
import com.smart.smartpay.ui.components.amountFromDigits
import com.smart.smartpay.ui.components.applyAmountKey
import com.smart.smartpay.ui.format.formatAmount

/**
 * Dialog to enter a free tip amount with the same keypad as the Sale screen.
 *
 * @param maxAmountInCents configured max tip (null = no limit); "Accept" is disabled above it.
 */
@Composable
fun CustomTipDialog(
    initialAmountInCents: Long,
    maxAmountInCents: Long?,
    onConfirm: (amountInCents: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var digits by rememberSaveable {
        mutableStateOf(if (initialAmountInCents > 0) initialAmountInCents.toString() else "")
    }
    val amountInCents = amountFromDigits(digits)
    val isAllowed = isCustomTipAllowed(amountInCents, maxAmountInCents)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tip_custom_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatAmount(amountInCents),
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isAllowed) Color.Unspecified else MaterialTheme.colorScheme.error,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                    maxAmountInCents?.let { max ->
                        Text(
                            text = stringResource(
                                if (isAllowed) R.string.tip_custom_dialog_max else R.string.tip_custom_dialog_exceeds,
                                formatAmount(max),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isAllowed) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                AmountKeypad(onKeyPressed = { key -> digits = applyAmountKey(digits, key) })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(amountInCents) }, enabled = isAllowed) {
                Text(stringResource(R.string.tip_custom_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.tip_custom_dialog_cancel))
            }
        },
    )
}
