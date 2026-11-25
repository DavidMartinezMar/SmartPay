package com.smart.smartpay.domain.authentication

/** Outcome of [AuthenticateProfilerUseCase]. */
sealed interface ProfilerAuthenticationResult {

    /** The terminal is authenticated, its configuration stored and the EMV module ready to sell. */
    data class Success(val token: String) : ProfilerAuthenticationResult

    /**
     * The terminal has more than one onboarding: show [options] to the user and call the use case
     * again with [ProfilerAuthenticationParams.onboardingId] set to the chosen [BoardingOption.id].
     */
    data class BoardingSelectionRequired(
        val options: List<BoardingOption>,
    ) : ProfilerAuthenticationResult

    /** Authentication failed. [code] is the local error code (see [ErrorCodes] for local ones). */
    data class Error(val code: String, val message: String) : ProfilerAuthenticationResult

    object ErrorCodes {
        /** [com.smart.smartpay.core.PaymentSdk.initialize] was not called. */
        const val NOT_INITIALIZED: String = "SDK_NOT_INITIALIZED"

        /** [com.smart.smartpay.core.PaymentSdk.requiredPermissions] are not granted. */
        const val PERMISSIONS_REQUIRED: String = "SDK_PERMISSIONS_REQUIRED"

        /** Authenticated, but storing the configuration or initializing the EMV module failed. */
        const val TERMINAL_SETUP_FAILED: String = "SDK_TERMINAL_SETUP_FAILED"

        /** The terminal serial number could not be read from the device. */
        const val SERIAL_NUMBER_UNAVAILABLE: String = "SDK_SERIAL_NUMBER_UNAVAILABLE"

        /** The authentication finished without reporting any result. */
        const val NO_RESPONSE: String = "SDK_NO_RESPONSE"
    }
}

/** An onboarding the user can choose from. */
data class BoardingOption(
    val id: String,
    val name: String,
    val description: String,
)
