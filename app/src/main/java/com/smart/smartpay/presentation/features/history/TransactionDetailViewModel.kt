package com.smart.smartpay.presentation.features.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.smart.smartpay.data.storage.repositories.TransactionRepository
import com.smart.smartpay.navigation.TransactionDetailRoute
import com.smart.smartpay.presentation.models.TransactionDetailUiState
import com.smart.smartpay.utils.STOP_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

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