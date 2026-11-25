package com.smart.smartpay.features.operation

// Commented out temporarily: cancellation/refund disabled while they depend on common.
// TODO: restore once cancellation/refund are migrated off common.
//
// import androidx.activity.compose.BackHandler
// import androidx.compose.foundation.layout.Arrangement
// import androidx.compose.foundation.layout.Column
// import androidx.compose.foundation.layout.Spacer
// import androidx.compose.foundation.layout.fillMaxSize
// import androidx.compose.foundation.layout.fillMaxWidth
// import androidx.compose.foundation.layout.height
// import androidx.compose.foundation.layout.padding
// import androidx.compose.material3.Button
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.OutlinedButton
// import androidx.compose.material3.Text
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.getValue
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.res.stringResource
// import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.text.style.TextAlign
// import androidx.compose.ui.unit.dp
// import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
// import androidx.lifecycle.compose.collectAsStateWithLifecycle
// import com.smart.myfinancialapp.R
// import com.smart.myfinancialapp.features.payment.CardOperationPromptDialog
// import com.smart.myfinancialapp.features.payment.CardProgress
// import com.smart.myfinancialapp.features.payment.PaymentResult
// import com.smart.myfinancialapp.features.payment.PinPad
// import com.smart.myfinancialapp.features.payment.ReceiptCard
// import com.smart.myfinancialapp.navigation.CardOperationType
// import com.smart.myfinancialapp.ui.format.formatAmount
//
// /**
//  * Cancellation or refund of a sale: status while the customer presents the card, card questions,
//  * PIN pad when typed on the app screen, and the result.
//  */
// @Composable
// fun CardOperationScreen(
//     onNewSale: () -> Unit,
//     onHistory: () -> Unit,
//     modifier: Modifier = Modifier,
//     viewModel: CardOperationViewModel = hiltViewModel(),
// ) {
//     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//     val isCancellation = uiState.type == CardOperationType.CANCELLATION
//
//     // Leaving mid-operation would lose its result: back only works once it finished
//     BackHandler(enabled = uiState.result == null) {}
//     BackHandler(enabled = uiState.result != null, onBack = onNewSale)
//
//     Column(
//         modifier = modifier
//             .fillMaxSize()
//             .padding(16.dp),
//         horizontalAlignment = Alignment.CenterHorizontally,
//     ) {
//         Text(
//             text = stringResource(if (isCancellation) R.string.operation_cancellation_title else R.string.operation_refund_title),
//             style = MaterialTheme.typography.titleLarge,
//         )
//         Text(
//             text = formatAmount(uiState.amountInCents),
//             style = MaterialTheme.typography.headlineMedium,
//             fontWeight = FontWeight.Bold,
//         )
//         Text(
//             text = stringResource(R.string.operation_present_card),
//             style = MaterialTheme.typography.bodyMedium,
//             color = MaterialTheme.colorScheme.onSurfaceVariant,
//             textAlign = TextAlign.Center,
//         )
//         Spacer(Modifier.weight(1f))
//
//         when (val result = uiState.result) {
//             null -> CardProgress(
//                 status = uiState.status,
//                 pinMessage = uiState.pinMessage,
//                 canCancel = uiState.canCancel,
//                 onCancel = viewModel::onCancel,
//             )
//             else -> OperationResult(
//                 isCancellation = isCancellation,
//                 result = result,
//                 onRetry = viewModel::onRetry,
//                 onNewSale = onNewSale,
//                 onHistory = onHistory,
//             )
//         }
//         Spacer(Modifier.weight(1f))
//     }
//
//     uiState.prompt?.let { CardOperationPromptDialog(it, onAnswered = viewModel::onPromptAnswered) }
//
//     uiState.pinPad?.let { PinPad(it, uiState.pinDigits, uiState.pinMessage) }
// }
//
// @Composable
// private fun OperationResult(
//     isCancellation: Boolean,
//     result: PaymentResult,
//     onRetry: () -> Unit,
//     onNewSale: () -> Unit,
//     onHistory: () -> Unit,
// ) {
//     Column(
//         modifier = Modifier.fillMaxWidth(),
//         horizontalAlignment = Alignment.CenterHorizontally,
//         verticalArrangement = Arrangement.spacedBy(16.dp),
//     ) {
//         when (result) {
//             is PaymentResult.Approved -> {
//                 Text(
//                     stringResource(if (isCancellation) R.string.operation_cancellation_approved else R.string.operation_refund_approved),
//                     style = MaterialTheme.typography.headlineSmall,
//                     color = MaterialTheme.colorScheme.primary,
//                     fontWeight = FontWeight.Bold,
//                 )
//                 ReceiptCard(result.receipt)
//             }
//
//             is PaymentResult.Declined, is PaymentResult.Failed -> {
//                 val (message, code) = when (result) {
//                     is PaymentResult.Declined -> result.message to result.code
//                     is PaymentResult.Failed -> result.message to result.code
//                     else -> "" to ""
//                 }
//                 Text(
//                     stringResource(if (isCancellation) R.string.operation_cancellation_declined else R.string.operation_refund_declined),
//                     style = MaterialTheme.typography.headlineSmall,
//                     color = MaterialTheme.colorScheme.error,
//                     fontWeight = FontWeight.Bold,
//                 )
//                 if (result is PaymentResult.Declined && result.retryWithChip) {
//                     Text(stringResource(R.string.payment_retry_with_chip), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
//                 }
//                 Text(stringResource(R.string.payment_error_detail, message, code), textAlign = TextAlign.Center)
//                 Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(56.dp)) {
//                     Text(stringResource(R.string.payment_retry))
//                 }
//             }
//         }
//         OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth().height(56.dp)) {
//             Text(stringResource(R.string.operation_view_history))
//         }
//         OutlinedButton(onClick = onNewSale, modifier = Modifier.fillMaxWidth().height(56.dp)) {
//             Text(stringResource(R.string.payment_new_sale))
//         }
//     }
// }
