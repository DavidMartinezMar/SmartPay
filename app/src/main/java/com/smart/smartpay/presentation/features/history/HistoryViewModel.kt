package com.smart.smartpay.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.smartpay.data.storage.model.TransactionRecord
import com.smart.smartpay.data.storage.repositories.TransactionRepository
import com.smart.smartpay.utils.STOP_TIMEOUT_MS
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
