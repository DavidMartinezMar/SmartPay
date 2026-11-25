package com.smart.smartpay.features.payment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.smart.smartpay.R
import com.smart.smartpay.domain.payment.CardDigitsType
import com.smart.smartpay.domain.payment.Decision
import com.smart.smartpay.domain.payment.DccChoice
import com.smart.smartpay.domain.payment.PromotionChoice
import com.smart.smartpay.domain.payment.SaleEvent

/**
 * Dialog for a sale question. Every option answers the SDK Decision, then calls [onAnswered].
 * None can be dismissed by tapping outside: the card reader is waiting for an answer.
 */
@Composable
fun PaymentPromptDialog(prompt: SaleEvent, onAnswered: () -> Unit) {
    when (prompt) {
        is SaleEvent.PromotionsOffered -> {
            val options = buildList {
                add(PromotionChoice.None to stringResource(R.string.payment_promotion_none))
                prompt.options.monthsWithoutInterest.forEach {
                    add(PromotionChoice.MonthsWithoutInterest(it) to stringResource(R.string.payment_promotion_msi, it))
                }
                prompt.options.monthsWithInterest.forEach {
                    add(PromotionChoice.MonthsWithInterest(it) to stringResource(R.string.payment_promotion_mci, it))
                }
                if (prompt.options.loyaltyPaymentAvailable) {
                    add(PromotionChoice.LoyaltyPayment to stringResource(R.string.payment_promotion_points))
                }
            }
            ChoiceDialog(stringResource(R.string.payment_promotions_title), options, prompt.decision, onAnswered)
        }

        is SaleEvent.DccOffered -> {
            val offer = prompt.offer
            ChoiceDialog(
                title = stringResource(R.string.payment_dcc_title),
                text = stringResource(R.string.payment_dcc_detail, offer.exchangeRate, offer.markUp),
                options = listOf(
                    DccChoice.FOREIGN_CURRENCY to stringResource(R.string.payment_dcc_foreign, offer.foreignAmount, offer.foreignCurrency),
                    DccChoice.LOCAL_CURRENCY to stringResource(R.string.payment_dcc_local, offer.localAmount),
                ),
                decision = prompt.decision,
                onAnswered = onAnswered,
            )
        }

        is SaleEvent.ApplicationSelectionRequired -> ChoiceDialog(
            title = stringResource(R.string.payment_application_title),
            options = prompt.applications.map { it to it.name },
            decision = prompt.decision,
            onAnswered = onAnswered,
        )

        is SaleEvent.CurrencyMismatch -> AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.payment_currency_title)) },
            text = {
                Text(
                    stringResource(
                        if (prompt.cardIsForeign) R.string.payment_currency_foreign_card else R.string.payment_currency_national_card,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = { prompt.decision.choose(true); onAnswered() }) {
                    Text(stringResource(R.string.payment_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { prompt.decision.cancel(); onAnswered() }) {
                    Text(stringResource(R.string.payment_cancel))
                }
            },
        )

        is SaleEvent.CardDigitsRequired -> CardDigitsDialog(prompt.type, prompt.decision, onAnswered)

        else -> Unit
    }
}

// Commented out temporarily: cancellation/refund disabled while they depend on common.
// TODO: restore once cancellation/refund are migrated off common.
//
// /** Card reading questions of a cancellation or refund (same dialogs as the sale). */
// @Composable
// fun CardOperationPromptDialog(prompt: CardOperationEvent, onAnswered: () -> Unit) {
//     when (prompt) {
//         is CardOperationEvent.ApplicationSelectionRequired -> ChoiceDialog(
//             title = stringResource(R.string.payment_application_title),
//             options = prompt.applications.map { it to it.name },
//             decision = prompt.decision,
//             onAnswered = onAnswered,
//         )
//
//         is CardOperationEvent.CardDigitsRequired -> CardDigitsDialog(prompt.type, prompt.decision, onAnswered)
//
//         else -> Unit
//     }
// }

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<Pair<T, String>>,
    decision: Decision<T>,
    onAnswered: () -> Unit,
    text: String? = null,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(title) },
        text = {
            Column {
                text?.let { Text(it, modifier = Modifier.padding(bottom = 8.dp)) }
                options.forEach { (value, label) ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { decision.choose(value); onAnswered() }
                            .padding(vertical = 14.dp),
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { decision.cancel(); onAnswered() }) {
                Text(stringResource(R.string.payment_cancel))
            }
        },
    )
}

@Composable
private fun CardDigitsDialog(type: CardDigitsType, decision: Decision<String>, onAnswered: () -> Unit) {
    var digits by rememberSaveable { mutableStateOf("") }
    val isCvv = type == CardDigitsType.CVV
    val validLength = if (isCvv) digits.length in 3..4 else digits.length == 4

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(stringResource(if (isCvv) R.string.payment_digits_cvv_title else R.string.payment_digits_last4_title))
        },
        text = {
            OutlinedTextField(
                value = digits,
                onValueChange = { value -> digits = value.filter { it.isDigit() }.take(4) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
            )
        },
        confirmButton = {
            TextButton(enabled = validLength, onClick = { decision.choose(digits); onAnswered() }) {
                Text(stringResource(R.string.payment_continue))
            }
        },
        dismissButton = {
            TextButton(onClick = { decision.cancel(); onAnswered() }) {
                Text(stringResource(R.string.payment_cancel))
            }
        },
    )
}
