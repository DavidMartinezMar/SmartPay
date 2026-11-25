package com.smart.smartpay.features.payment

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.smart.smartpay.data.transactions.TransactionRepository
import com.smart.smartpay.domain.payment.Intercard
import com.smart.smartpay.navigation.PaymentRoute
import com.smart.smartpay.domain.payment.SaleEvent
import com.smart.smartpay.domain.payment.SaleReceipt
import com.smart.smartpay.domain.payment.SaleRequest
import com.smart.smartpay.domain.payment.SaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** What the terminal is doing while the sale runs. */
sealed interface PaymentStatus {
    data object Starting : PaymentStatus
    data object WaitingForCard : PaymentStatus
    data object ReadingCard : PaymentStatus
    data class CardDetected(val label: String) : PaymentStatus
    data class Message(val text: String) : PaymentStatus
    data object EnteringPin : PaymentStatus
    data object Processing : PaymentStatus
}

sealed interface PaymentResult {
    data class Approved(val receipt: SaleReceipt) : PaymentResult

    /** [retryWithChip]: contactless failed, retry inserting the card. */
    data class Declined(val message: String, val code: String, val retryWithChip: Boolean) : PaymentResult

    data class Failed(val message: String, val code: String) : PaymentResult

    data class IntercardCard(val card: Intercard) : PaymentResult
}

data class PaymentUiState(
    val amountInCents: Long,
    val tipInCents: Long,
    val status: PaymentStatus = PaymentStatus.Starting,
    /** Question waiting for the customer (a SaleEvent carrying a Decision). */
    val prompt: SaleEvent? = null,
    val pinDigits: Int = 0,
    val pinMessage: String? = null,
    val result: PaymentResult? = null,
    /** History id of the approved sale, once recorded (needed to cancel / refund it). */
    val transactionId: Long? = null,
) {
    val totalInCents: Long get() = amountInCents + tipInCents
    val canCancel: Boolean get() = result == null && status == PaymentStatus.WaitingForCard
}

/** Runs the card sale with the Smart SDK, turns its events into UI state and records approved sales. */
@HiltViewModel
class PaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sale: SaleUseCase,
    private val transactions: TransactionRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<PaymentRoute>()

    private val _uiState = MutableStateFlow(PaymentUiState(route.amountInCents, route.tipInCents))
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private var saleJob: Job? = null
    private var retryWithChip = false

    init {
        startSale()
    }

    /** Same amounts again (e.g. after a decline or "retry with chip"). */
    fun onRetry() = startSale()

    fun onCancel() {
        viewModelScope.launch { sale.cancel() }
    }

    /** The UI answered the current prompt (the Decision itself was already resolved). */
    fun onPromptAnswered() {
        _uiState.update { it.copy(prompt = null) }
    }

    private fun startSale() {
        saleJob?.cancel()
        retryWithChip = false
        _uiState.value = PaymentUiState(route.amountInCents, route.tipInCents)
        // Collected in viewModelScope: keeps running across rotation until the result arrives
        saleJob = viewModelScope.launch {
            sale(SaleRequest(amountInCents = route.amountInCents, tipInCents = route.tipInCents))
                .collect(::onEvent)
        }
    }

    /** The approved sale goes to the history; its id enables cancel / refund on the receipt. */
    private fun record(receipt: SaleReceipt) {
        viewModelScope.launch {
            runCatching { transactions.saveSale(receipt, route.amountInCents, route.tipInCents) }
                .onSuccess { id -> _uiState.update { it.copy(transactionId = id) } }
                .onFailure { Log.e(TAG, "Approved sale not recorded in the history", it) }
        }
    }

    private fun onEvent(event: SaleEvent) {
        if (event is SaleEvent.Approved) record(event.receipt)
        _uiState.update { state ->
            // A new event means a pending prompt was answered or timed out
            val current = state.copy(prompt = null)
            when (event) {
                SaleEvent.WaitingForCard -> current.copy(status = PaymentStatus.WaitingForCard)
                SaleEvent.CardRead -> current.copy(status = PaymentStatus.ReadingCard)
                is SaleEvent.CardDetected -> current.copy(status = PaymentStatus.CardDetected(event.label))
                is SaleEvent.Message -> current.copy(status = PaymentStatus.Message(event.text))
                SaleEvent.RetryWithChip -> {
                    retryWithChip = true
                    current
                }

                is SaleEvent.ApplicationSelectionRequired,
                is SaleEvent.CardDigitsRequired,
                is SaleEvent.CurrencyMismatch,
                is SaleEvent.PromotionsOffered,
                is SaleEvent.DccOffered,
                -> current.copy(prompt = event)

                SaleEvent.PinRequested -> current.copy(
                    status = PaymentStatus.EnteringPin,
                    pinDigits = 0,
                    pinMessage = null,
                )
                is SaleEvent.PinDigitsEntered -> current.copy(pinDigits = event.count)
                is SaleEvent.PinMessage -> current.copy(pinMessage = event.text)
                is SaleEvent.PinError -> current.copy(pinMessage = event.message)
                SaleEvent.PinProcessing -> current.copy(status = PaymentStatus.Processing)

                is SaleEvent.Approved -> current.copy(result = PaymentResult.Approved(event.receipt))
                is SaleEvent.Declined -> current.copy(
                    result = PaymentResult.Declined(
                        message = event.errorMessage.ifBlank { event.responseMessage },
                        code = event.errorCode.ifBlank { event.responseCode },
                        retryWithChip = retryWithChip,
                    ),
                )
                is SaleEvent.Failed -> current.copy(result = PaymentResult.Failed(event.message, event.code))
                is SaleEvent.IntercardRead -> current.copy(result = PaymentResult.IntercardCard(event.intercard))
            }
        }
    }

    private companion object {
        const val TAG = "Payment"
    }
}
