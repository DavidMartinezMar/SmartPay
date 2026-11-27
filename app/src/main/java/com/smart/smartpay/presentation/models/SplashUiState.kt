package com.smart.smartpay.presentation.models

import com.smart.smartpay.domain.models.BoardingOption

sealed interface SplashUiState {
    data object Loading : SplashUiState

    /** Terminal authenticated: continue to the app. */
    data object Ready : SplashUiState

    /** The terminal has several onboardings: the user must pick one. */
    data class BoardingSelection(val options: List<BoardingOption>) : SplashUiState

    data class Error(val code: String, val message: String) : SplashUiState

    /** The SDK permissions were denied: the terminal can't be configured. */
    data object PermissionsDenied : SplashUiState
}