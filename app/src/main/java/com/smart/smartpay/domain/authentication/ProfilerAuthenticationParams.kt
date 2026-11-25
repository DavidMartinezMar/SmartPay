package com.smart.smartpay.domain.authentication

/**
 * Data to authenticate the terminal against the profiler. The terminal serial number is read
 * from the device, so it is not part of these params.
 *
 * @property branchKey branch node identifier, if already known.
 * @property smartKey POS node identifier, if already known.
 * @property isThirdPartyFlow true when running inside a third-party app flow.
 * @property onboardingId onboarding chosen by the user, required after
 * [ProfilerAuthenticationResult.BoardingSelectionRequired].
 */
data class ProfilerAuthenticationParams(
    val branchKey: String? = null,
    val smartKey: String? = null,
    val isThirdPartyFlow: Boolean = false,
    val onboardingId: String? = null,
)
