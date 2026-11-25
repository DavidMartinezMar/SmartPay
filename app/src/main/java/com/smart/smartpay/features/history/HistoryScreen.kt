package com.smart.smartpay.features.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smart.smartpay.R
import com.smart.smartpay.data.transactions.model.TransactionRecord
import com.smart.smartpay.data.transactions.model.TransactionType
import com.smart.smartpay.ui.format.formatAmount
import java.text.DateFormat
import java.util.Date

/** Approved transactions of this terminal, newest first. Tapping one opens its detail. */
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onTransactionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.history_back)) }
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        val transactions = uiState.transactions
        when {
            transactions == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            transactions.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.history_empty), textAlign = TextAlign.Center)
            }
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionRow(transaction, onClick = { onTransactionSelected(transaction.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: TransactionRecord, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(transaction.typeLabel()), style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${transaction.cardBrand} ${transaction.maskedPan.takeLast(4)}".trim(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatDateTime(transaction.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = transaction.signedAmount(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.SALE) Color.Unspecified else MaterialTheme.colorScheme.error,
            )
            transaction.statusLabel()?.let {
                Text(stringResource(it), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

internal fun TransactionRecord.typeLabel(): Int = when (type) {
    TransactionType.SALE -> R.string.history_type_sale
    TransactionType.CANCELLATION -> R.string.history_type_cancellation
    TransactionType.REFUND -> R.string.history_type_refund
}

/** Sales positive; cancellations and refunds give money back. */
internal fun TransactionRecord.signedAmount(): String =
    if (type == TransactionType.SALE) formatAmount(totalInCents) else "-${formatAmount(totalInCents)}"

/** Status of a sale after operations on it, if any. */
internal fun TransactionRecord.statusLabel(): Int? = when {
    type != TransactionType.SALE -> null
    isCancelled -> R.string.history_status_cancelled
    refundedInCents >= totalInCents -> R.string.history_status_refunded
    refundedInCents > 0 -> R.string.history_status_partially_refunded
    else -> null
}

internal fun formatDateTime(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))
