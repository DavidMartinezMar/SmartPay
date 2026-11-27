package com.smart.smartpay.presentation.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.smartpay.domain.authentication.AuthenticateProfilerUseCase
import com.smart.smartpay.data.models.ProfilerAuthenticationParams
import com.smart.smartpay.domain.models.BoardingOption
import com.smart.smartpay.domain.models.ProfilerAuthenticationResult
import com.smart.smartpay.presentation.models.SplashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
