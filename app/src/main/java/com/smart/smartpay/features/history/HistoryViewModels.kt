package com.smart.smartpay.features.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.smart.smartpay.data.transactions.TransactionRepository
import com.smart.smartpay.data.transactions.model.TransactionRecord
import com.smart.smartpay.navigation.TransactionDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Null while loading. */
data class HistoryUiState(val transactions: List<TransactionRecord>? = null)

@HiltViewModel
class HistoryViewModel @Inject constructor(transactions: TransactionRepository) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = transactions.observeHistory()
        .map { HistoryUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HistoryUiState())
}

sealed interface TransactionDetailUiState {
    data object Loading : TransactionDetailUiState
    data object NotFound : TransactionDetailUiState

    /** [canCancel] is evaluated when the transaction is loaded (cancellation is same-day only). */
    data class Loaded(val transaction: TransactionRecord, val canCancel: Boolean) : TransactionDetailUiState
}

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    transactions: TransactionRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<TransactionDetailRoute>()

    val uiState: StateFlow<TransactionDetailUiState> = transactions.observeTransaction(route.transactionId)
        .map { record ->
            if (record == null) {
                TransactionDetailUiState.NotFound
            } else {
                TransactionDetailUiState.Loaded(record, record.canCancel(now = System.currentTimeMillis()))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TransactionDetailUiState.Loading)
}

private const val STOP_TIMEOUT_MS = 5_000L
