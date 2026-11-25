package com.smart.smartpay.features.operation

// Commented out temporarily: cancellation/refund disabled while they depend on common.
// TODO: restore once cancellation/refund are migrated off common.
//
// import android.util.Log
// import androidx.lifecycle.SavedStateHandle
// import androidx.lifecycle.ViewModel
// import androidx.lifecycle.viewModelScope
// import androidx.navigation.toRoute
// import com.smart.myfinancialapp.data.transactions.TransactionRepository
// import com.smart.myfinancialapp.data.transactions.model.TransactionRecord
// import com.smart.myfinancialapp.data.transactions.model.TransactionType
// import com.smart.myfinancialapp.features.payment.PaymentResult
// import com.smart.myfinancialapp.features.payment.PaymentStatus
// import com.smart.myfinancialapp.navigation.CardOperationRoute
// import com.smart.myfinancialapp.navigation.CardOperationType
// import com.smart.myfinancialapp.domain.operation.CancellationRequest
// import com.smart.myfinancialapp.domain.operation.CancellationUseCase
// import com.smart.myfinancialapp.domain.operation.CardOperationEvent
// import com.smart.myfinancialapp.domain.operation.RefundRequest
// import com.smart.myfinancialapp.domain.operation.RefundUseCase
// import com.smart.myfinancialapp.domain.payment.SaleCurrency
// import com.smart.myfinancialapp.domain.payment.SaleReceipt
// import com.smart.myfinancialapp.ui.pinpad.PinPadLayout
// import dagger.hilt.android.lifecycle.HiltViewModel
// import kotlinx.coroutines.Job
// import kotlinx.coroutines.flow.Flow
// import kotlinx.coroutines.flow.MutableStateFlow
// import kotlinx.coroutines.flow.StateFlow
// import kotlinx.coroutines.flow.asStateFlow
// import kotlinx.coroutines.flow.update
// import kotlinx.coroutines.launch
// import javax.inject.Inject
//
// data class CardOperationUiState(
//     val type: CardOperationType,
//     val amountInCents: Long,
//     val status: PaymentStatus = PaymentStatus.Starting,
//     /** Card reading question waiting for the customer (an event carrying a Decision). */
//     val prompt: CardOperationEvent? = null,
//     /** Set while the PIN is typed on the app screen. */
//     val pinPad: PinPadLayout? = null,
//     val pinDigits: Int = 0,
//     val pinMessage: String? = null,
//     val result: PaymentResult? = null,
// ) {
//     val canCancel: Boolean get() = result == null && status == PaymentStatus.WaitingForCard
// }
//
// /**
//  * Runs the cancellation or refund of a sale of the history with the Smart SDK, turns its events
//  * into UI state and records the approved operation in the history.
//  */
// @HiltViewModel
// class CardOperationViewModel @Inject constructor(
//     savedStateHandle: SavedStateHandle,
//     private val cancellation: CancellationUseCase,
//     private val refund: RefundUseCase,
//     private val transactions: TransactionRepository,
// ) : ViewModel() {
//
//     private val route = savedStateHandle.toRoute<CardOperationRoute>()
//
//     private val _uiState = MutableStateFlow(initialState())
//     val uiState: StateFlow<CardOperationUiState> = _uiState.asStateFlow()
//
//     private var operationJob: Job? = null
//     private var retryWithChip = false
//
//     init {
//         start()
//     }
//
//     /** Same operation again (e.g. after a decline or "retry with chip"). */
//     fun onRetry() = start()
//
//     fun onCancel() {
//         viewModelScope.launch {
//             when (route.type) {
//                 CardOperationType.CANCELLATION -> cancellation.cancel()
//                 CardOperationType.REFUND -> refund.cancel()
//             }
//         }
//     }
//
//     /** The UI answered the current prompt (the Decision itself was already resolved). */
//     fun onPromptAnswered() {
//         _uiState.update { it.copy(prompt = null) }
//     }
//
//     private fun initialState() = CardOperationUiState(route.type, route.amountInCents)
//
//     private fun start() {
//         operationJob?.cancel()
//         retryWithChip = false
//         _uiState.value = initialState()
//         // Collected in viewModelScope: keeps running across rotation until the result arrives
//         operationJob = viewModelScope.launch {
//             val sale = transactions.getTransaction(route.transactionId)
//             val operation = sale?.let { runCatching { operation(it) }.getOrNull() }
//             if (operation == null) {
//                 onEvent(CardOperationEvent.Failed(SALE_NOT_AVAILABLE, "La venta no está disponible para esta operación"))
//                 return@launch
//             }
//             operation.collect(::onEvent)
//         }
//     }
//
//     /** SDK request built from the sale of the history. Throws if the sale lacks its references. */
//     private fun operation(sale: TransactionRecord): Flow<CardOperationEvent> {
//         val currency = SaleCurrency.entries.firstOrNull { it.name == sale.currency } ?: SaleCurrency.MXN
//         return when (route.type) {
//             CardOperationType.CANCELLATION -> cancellation(
//                 CancellationRequest(
//                     reference = sale.reference,
//                     uniqueReference = sale.uniqueReference,
//                     currency = currency,
//                     transToken = sale.transToken,
//                 ),
//             )
//             CardOperationType.REFUND -> refund(
//                 RefundRequest(
//                     amountInCents = route.amountInCents,
//                     approvalCode = sale.authorizationCode,
//                     uniqueReference = sale.uniqueReference,
//                     currency = currency,
//                     transToken = sale.transToken,
//                 ),
//             )
//         }
//     }
//
//     private fun onEvent(event: CardOperationEvent) {
//         if (event is CardOperationEvent.Approved) record(event.receipt)
//         _uiState.update { state ->
//             // A new event means a pending prompt was answered or timed out
//             val current = state.copy(prompt = null)
//             when (event) {
//                 CardOperationEvent.WaitingForCard -> current.copy(status = PaymentStatus.WaitingForCard)
//                 CardOperationEvent.CardRead -> current.copy(status = PaymentStatus.ReadingCard)
//                 is CardOperationEvent.CardDetected -> current.copy(status = PaymentStatus.CardDetected(event.label))
//                 is CardOperationEvent.Message -> current.copy(status = PaymentStatus.Message(event.text))
//                 CardOperationEvent.RetryWithChip -> {
//                     retryWithChip = true
//                     current
//                 }
//
//                 is CardOperationEvent.ApplicationSelectionRequired,
//                 is CardOperationEvent.CardDigitsRequired,
//                 -> current.copy(prompt = event)
//
//                 is CardOperationEvent.PinRequested -> current.copy(
//                     status = PaymentStatus.EnteringPin,
//                     pinPad = event.pinPad,
//                     pinDigits = 0,
//                     pinMessage = null,
//                 )
//                 is CardOperationEvent.PinDigitsEntered -> current.copy(pinDigits = event.count)
//                 is CardOperationEvent.PinMessage -> current.copy(pinMessage = event.text)
//                 is CardOperationEvent.PinError -> current.copy(pinMessage = event.message)
//                 CardOperationEvent.PinProcessing -> current.copy(status = PaymentStatus.Processing, pinPad = null)
//
//                 is CardOperationEvent.Approved -> current.copy(pinPad = null, result = PaymentResult.Approved(event.receipt))
//                 is CardOperationEvent.Declined -> current.copy(pinPad = null, result = declined(event))
//                 is CardOperationEvent.Failed -> current.copy(pinPad = null, result = PaymentResult.Failed(event.message, event.code))
//             }
//         }
//     }
//
//     /** The approved operation goes to the history, linked to its sale. */
//     private fun record(receipt: SaleReceipt) {
//         val type = when (route.type) {
//             CardOperationType.CANCELLATION -> TransactionType.CANCELLATION
//             CardOperationType.REFUND -> TransactionType.REFUND
//         }
//         viewModelScope.launch {
//             runCatching { transactions.saveOperation(route.transactionId, type, receipt, route.amountInCents) }
//                 .onFailure { Log.e(TAG, "Approved $type not recorded in the history", it) }
//         }
//     }
//
//     private fun declined(event: CardOperationEvent.Declined): PaymentResult.Declined =
//         PaymentResult.Declined(
//             message = event.errorMessage.ifBlank { event.responseMessage },
//             code = event.errorCode.ifBlank { event.responseCode },
//             retryWithChip = retryWithChip,
//         )
//
//     private companion object {
//         const val TAG = "CardOperation"
//         const val SALE_NOT_AVAILABLE = "APP_SALE_NOT_AVAILABLE"
//     }
// }
