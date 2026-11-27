package com.smart.smartpay.presentation.models

import com.smart.smartpay.data.storage.model.TransactionRecord

sealed interface TransactionDetailUiState {
    data object Loading : TransactionDetailUiState
    data object NotFound : TransactionDetailUiState

    /** [canCancel] is evaluated when the transaction is loaded (cancellation is same-day only). */
    data class Loaded(val transaction: TransactionRecord, val canCancel: Boolean) : TransactionDetailUiState
}