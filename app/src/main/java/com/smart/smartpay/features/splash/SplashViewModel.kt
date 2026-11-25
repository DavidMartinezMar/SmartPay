package com.smart.smartpay.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.smartpay.domain.authentication.AuthenticateProfilerUseCase
import com.smart.smartpay.domain.authentication.BoardingOption
import com.smart.smartpay.domain.authentication.ProfilerAuthenticationParams
import com.smart.smartpay.domain.authentication.ProfilerAuthenticationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

/** Startup work behind the splash: authenticates the terminal with the Smart SDK (profiler). */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authenticateProfiler: AuthenticateProfilerUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    // Kept so a retry after choosing an onboarding uses the same choice
    private var onboardingId: String? = null
    private var started = false

    /** Called by the screen once the SDK permissions are granted. Runs once (survives rotation). */
    fun onPermissionsGranted() {
        if (started) return
        started = true
        authenticate()
    }

    fun onPermissionsDenied() {
        _uiState.value = SplashUiState.PermissionsDenied
    }

    fun onRetry() = authenticate()

    fun onBoardingSelected(option: BoardingOption) {
        onboardingId = option.id
        authenticate()
    }

    private fun authenticate() {
        _uiState.value = SplashUiState.Loading
        viewModelScope.launch {
            val result = authenticateProfiler(ProfilerAuthenticationParams(onboardingId = onboardingId))
            _uiState.value = when (result) {
                is ProfilerAuthenticationResult.Success -> SplashUiState.Ready
                is ProfilerAuthenticationResult.BoardingSelectionRequired -> SplashUiState.BoardingSelection(result.options)
                is ProfilerAuthenticationResult.Error -> SplashUiState.Error(result.code, result.message)
            }
        }
    }
}
