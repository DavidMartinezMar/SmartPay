package com.smart.smartpay.presentation.ui.screens.payment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smart.smartpay.R
import com.smart.smartpay.domain.models.Intercard
import com.smart.smartpay.domain.models.SaleReceipt
import com.smart.smartpay.presentation.features.payment.PaymentViewModel
import com.smart.smartpay.presentation.models.PaymentResult
import com.smart.smartpay.presentation.models.PaymentStatus
import com.smart.smartpay.presentation.utils.formatAmount

/**
 * Card payment: sale status, customer questions (promotions, DCC, ...), PIN pad when the PIN is
 * typed on the app screen, and the result.
 */
@Composable
fun PaymentScreen(
    onNewSale: () -> Unit,
    /** Cancel the approved sale: its history id and total in cents. */
    onCancelSale: (transactionId: Long, totalInCents: Long) -> Unit,
    /** Refund the approved sale's total: its history id and the amount in cents. */
    onRefund: (transactionId: Long, amountInCents: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Leaving mid-sale would lose its result: back only works once it finished
    BackHandler(enabled = uiState.result == null) {}
    BackHandler(enabled = uiState.result != null, onBack = onNewSale)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.payment_total, formatAmount(uiState.totalInCents)),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        if (uiState.tipInCents > 0) {
            Text(
                text = stringResource(R.string.payment_tip_included, formatAmount(uiState.tipInCents)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.weight(1f))

        when (val result = uiState.result) {
            null -> CardProgress(
                status = uiState.status,
                pinMessage = uiState.pinMessage,
                canCancel = uiState.canCancel,
                onCancel = viewModel::onCancel,
            )
            else -> SaleResult(
                result = result,
                onRetry = viewModel::onRetry,
                onNewSale = onNewSale,
                // Available once the sale is in the history
                onCancelSale = uiState.transactionId?.let { id -> { onCancelSale(id, uiState.totalInCents) } },
                onRefund = uiState.transactionId?.let { id -> { onRefund(id, uiState.totalInCents) } },
            )
        }
        Spacer(Modifier.weight(1f))
    }

    uiState.prompt?.let { PaymentPromptDialog(it, onAnswered = viewModel::onPromptAnswered) }
}

/** Card transaction in progress (sale, cancellation or refund). */
@Composable
internal fun CardProgress(status: PaymentStatus, pinMessage: String?, canCancel: Boolean, onCancel: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        CircularProgressIndicator()
        Text(
            text = when (status) {
                PaymentStatus.Starting -> stringResource(R.string.payment_status_starting)
                PaymentStatus.WaitingForCard -> stringResource(R.string.payment_status_waiting_card)
                PaymentStatus.ReadingCard -> stringResource(R.string.payment_status_reading)
                is PaymentStatus.CardDetected -> status.label
                is PaymentStatus.Message -> status.text
                PaymentStatus.EnteringPin -> pinMessage ?: stringResource(R.string.payment_status_pin)
                PaymentStatus.Processing -> stringResource(R.string.payment_status_processing)
            },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        if (canCancel) {
            OutlinedButton(onClick = onCancel) { Text(stringResource(R.string.payment_cancel)) }
        }
    }
}

@Composable
private fun SaleResult(
    result: PaymentResult,
    onRetry: () -> Unit,
    onNewSale: () -> Unit,
    onCancelSale: (() -> Unit)?,
    onRefund: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (result) {
            is PaymentResult.Approved -> {
                Text(
                    stringResource(R.string.payment_approved),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                ReceiptCard(result.receipt)
                Button(onClick = onNewSale, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text(stringResource(R.string.payment_new_sale))
                }
                // Cancellation / refund need the sale's references
                if (onCancelSale != null && onRefund != null && result.receipt.uniqueReference.isNotBlank()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onCancelSale, modifier = Modifier.weight(1f).height(56.dp)) {
                            Text(stringResource(R.string.payment_cancel_sale))
                        }
                        OutlinedButton(onClick = onRefund, modifier = Modifier.weight(1f).height(56.dp)) {
                            Text(stringResource(R.string.payment_refund_sale))
                        }
                    }
                }
            }

            is PaymentResult.Declined, is PaymentResult.Failed -> {
                val (message, code) = when (result) {
                    is PaymentResult.Declined -> result.message to result.code
                    is PaymentResult.Failed -> result.message to result.code
                    else -> "" to ""
                }
                Text(
                    stringResource(R.string.payment_declined),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
                if (result is PaymentResult.Declined && result.retryWithChip) {
                    Text(stringResource(R.string.payment_retry_with_chip), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                }
                Text(stringResource(R.string.payment_error_detail, message, code), textAlign = TextAlign.Center)
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text(stringResource(R.string.payment_retry))
                }
                OutlinedButton(onClick = onNewSale, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text(stringResource(R.string.payment_new_sale))
                }
            }

            is PaymentResult.IntercardCard -> {
                Text(
                    stringResource(R.string.payment_mifare_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Intercard(result.card)
                Button(onClick = onNewSale, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text(stringResource(R.string.payment_new_sale))
                }
            }
        }
    }
}

/** Main data of a card transaction receipt. */
@Composable
internal fun ReceiptCard(receipt: SaleReceipt) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ReceiptLine(R.string.payment_receipt_auth, receipt.authorizationCode)
            ReceiptLine(R.string.payment_receipt_reference, receipt.reference)
            ReceiptLine(R.string.payment_receipt_card, "${receipt.cardBrand} ${receipt.maskedPan}".trim())
            ReceiptLine(R.string.payment_receipt_date, "${receipt.date} ${receipt.time}".trim())
        }
    }
}

@Composable
private fun ReceiptLine(label: Int, value: String) {
    if (value.isBlank()) return
    Text("${stringResource(label)}: $value", style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun Intercard(card: Intercard) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ReceiptLine(
                R.string.payment_mifare_technology,
                card.technology
            )
            ReceiptLine(
                R.string.payment_mifare_uid,
                card.uid
            )
            ReceiptLine(
                R.string.payment_mifare_sak,
                card.sak
            )
            ReceiptLine(
                R.string.payment_mifare_atqa,
                card.atqa
            )
            ReceiptLine(
                R.string.payment_mifare_site_code,
                card.siteCode
            )
            ReceiptLine(
                R.string.payment_mifare_account,
                card.accountNumber
            )
            ReceiptLine(
                R.string.payment_mifare_encrypted_account,
                card.encryptedAccountNumber
            )
        }
    }
}
