package com.smart.smartpay.presentation.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smart.smartpay.R
import com.smart.smartpay.data.storage.model.TransactionRecord
import com.smart.smartpay.data.storage.model.TransactionType
import com.smart.smartpay.data.utils.extensionfunctions.signedAmount
import com.smart.smartpay.data.utils.extensionfunctions.statusLabel
import com.smart.smartpay.data.utils.extensionfunctions.typeLabel
import com.smart.smartpay.presentation.features.history.TransactionDetailViewModel
import com.smart.smartpay.presentation.models.TransactionDetailUiState
import com.smart.smartpay.presentation.utils.formatAmount
import com.smart.smartpay.utils.MAX_DIGITS
import com.smart.smartpay.utils.amountFromDigits
import com.smart.smartpay.utils.formatDateTime

/** A transaction of the history; for a sale, its operations and the cancel / refund actions. */
@Composable
fun TransactionDetailScreen(
    onBack: () -> Unit,
    onCancelSale: (transactionId: Long, totalInCents: Long) -> Unit,
    onRefund: (transactionId: Long, amountInCents: Long) -> Unit,
    onTransactionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRefundDialog by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.history_back)) }
            Text(
                text = stringResource(R.string.detail_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        when (val state = uiState) {
            TransactionDetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            TransactionDetailUiState.NotFound -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.detail_not_found))
            }
            is TransactionDetailUiState.Loaded -> {
                val transaction = state.transaction
                Detail(
                    transaction = transaction,
                    canCancel = state.canCancel,
                    onCancelSale = { onCancelSale(transaction.id, transaction.totalInCents) },
                    onRefund = { showRefundDialog = true },
                    onOriginalSelected = { transaction.originalId?.let(onTransactionSelected) },
                    modifier = Modifier.weight(1f),
                )
                if (showRefundDialog) {
                    RefundAmountDialog(
                        refundableInCents = transaction.refundableInCents,
                        onConfirm = { amount ->
                            showRefundDialog = false
                            onRefund(transaction.id, amount)
                        },
                        onDismiss = { showRefundDialog = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun Detail(
    transaction: TransactionRecord,
    canCancel: Boolean,
    onCancelSale: () -> Unit,
    onRefund: () -> Unit,
    onOriginalSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(transaction.typeLabel()), style = MaterialTheme.typography.titleMedium)
            Text(transaction.signedAmount(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            transaction.statusLabel()?.let {
                Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleSmall)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Line(R.string.detail_date, formatDateTime(transaction.createdAt))
                Line(R.string.payment_receipt_card, "${transaction.cardBrand} ${transaction.maskedPan}".trim())
                Line(R.string.payment_receipt_auth, transaction.authorizationCode)
                Line(R.string.payment_receipt_reference, transaction.reference)
                if (transaction.tipInCents > 0) Line(R.string.detail_tip, formatAmount(transaction.tipInCents))
                if (transaction.refundedInCents > 0) Line(R.string.detail_refunded, formatAmount(transaction.refundedInCents))
            }
        }

        if (transaction.operations.isNotEmpty()) {
            Text(stringResource(R.string.detail_operations), style = MaterialTheme.typography.titleMedium)
            transaction.operations.forEach { operation ->
                Text(
                    "${stringResource(operation.typeLabel())} ${operation.signedAmount()} · ${formatDateTime(operation.createdAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (transaction.type == TransactionType.SALE) {
            if (canCancel) {
                Button(onClick = onCancelSale, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text(stringResource(R.string.payment_cancel_sale))
                }
            }
            if (transaction.canRefund) {
                OutlinedButton(onClick = onRefund, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text(stringResource(R.string.payment_refund_sale))
                }
            }
            if (!canCancel && !transaction.canRefund) {
                Text(stringResource(R.string.detail_no_actions), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            OutlinedButton(onClick = onOriginalSelected, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text(stringResource(R.string.detail_view_sale))
            }
        }
    }
}

@Composable
private fun Line(label: Int, value: String) {
    if (value.isBlank()) return
    Text("${stringResource(label)}: $value", style = MaterialTheme.typography.bodyLarge)
}

/** Amount to refund, POS-style in cents (typing 1, 2, 5 is $1.25), up to [refundableInCents]. */
@Composable
private fun RefundAmountDialog(refundableInCents: Long, onConfirm: (Long) -> Unit, onDismiss: () -> Unit) {
    var digits by rememberSaveable { mutableStateOf(refundableInCents.toString()) }
    val amount = amountFromDigits(digits)
    val valid = amount in 1..refundableInCents

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_refund_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.detail_refund_dialog_max, formatAmount(refundableInCents)))
                OutlinedTextField(
                    value = digits,
                    onValueChange = { value -> digits = value.filter { it.isDigit() }.take(MAX_DIGITS) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text(formatAmount(amount)) },
                    isError = !valid,
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onConfirm(amount) }) {
                Text(stringResource(R.string.payment_continue))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.payment_cancel)) }
        },
    )
}
