package com.smart.smartpay.domain.models

/** Outcome of [com.smart.smartpay.domain.authentication.AuthenticateProfilerUseCase]. */
sealed interface ProfilerAuthenticationResult {

    /** The terminal is authenticated, its configuration stored and the EMV module ready to sell. */
    data class Success(val token: String) : ProfilerAuthenticationResult

    /**
     * The terminal has more than one onboarding: show [options] to the user and call the use case
     * again with [com.smart.smartpay.data.models.ProfilerAuthenticationParams.onboardingId] set to the chosen [BoardingOption.id].
     */
    data class BoardingSelectionRequired(
        val options: List<BoardingOption>,
    ) : ProfilerAuthenticationResult

    /** Authentication failed. [code] is the local error code (see [ErrorCodes] for local ones). */
    data class Error(val code: String, val message: String) : ProfilerAuthenticationResult
}
