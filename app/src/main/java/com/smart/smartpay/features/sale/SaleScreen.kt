package com.smart.smartpay.features.sale

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.smart.smartpay.R
import com.smart.smartpay.ui.components.AmountKeypad
import com.smart.smartpay.ui.components.amountFromDigits
import com.smart.smartpay.ui.components.applyAmountKey
import com.smart.smartpay.ui.format.formatAmount
import com.smart.smartpay.ui.theme.SmartPayAppTheme

/**
 * Sale screen: numeric keypad to capture an amount and a "Charge" button.
 *
 * The amount is captured POS-style, in cents: each digit enters from the right
 * (typing 1, 2, 5 shows $1.25).
 *
 * @param onCharge invoked with the amount in cents when the user taps "Charge", and whether
 * the customer must be asked for a tip (tipProcessing of the terminal).
 * @param onHistory opens the transaction history (to cancel or refund a sale).
 */
@Composable
fun SaleScreen(
    onCharge: (amountInCents: Long, askForTip: Boolean) -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SaleViewModel = hiltViewModel(),
) {
    var digits by rememberSaveable { mutableStateOf("") }

    SaleContent(
        amountInCents = amountFromDigits(digits),
        onKeyPressed = { key -> digits = applyAmountKey(digits, key) },
        onCharge = { amountInCents -> onCharge(amountInCents, viewModel.isTipOffered()) },
        onHistory = onHistory,
        modifier = modifier,
    )
}

@Composable
private fun SaleContent(
    amountInCents: Long,
    onKeyPressed: (String) -> Unit,
    onCharge: (Long) -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextButton(onClick = onHistory, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.sale_history_button))
        }
        Text(
            text = stringResource(R.string.sale_amount_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = formatAmount(amountInCents),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Spacer(Modifier.weight(1f))

        AmountKeypad(onKeyPressed = onKeyPressed)

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onCharge(amountInCents) },
            enabled = amountInCents > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                text = stringResource(R.string.sale_charge_button, formatAmount(amountInCents)),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun SaleScreenPreview() {
    SmartPayAppTheme {
        SaleContent(amountInCents = 12550, onKeyPressed = {}, onCharge = {}, onHistory = {})
    }
}
