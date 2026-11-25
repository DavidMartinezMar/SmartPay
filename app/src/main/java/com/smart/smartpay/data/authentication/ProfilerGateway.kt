package com.smart.smartpay.data.authentication

import com.smart.core.SmartCore
import com.smart.core.business.model.AuthenticationProfilerResult
import com.smart.core.core.model.AuthenticationProfilerParam
import com.smart.core.core.model.SmartError
import com.smart.smartpay.domain.authentication.BoardingOption
import com.smart.smartpay.domain.authentication.ProfilerAuthenticationParams
import com.smart.smartpay.domain.authentication.ProfilerAuthenticationResult
import kotlinx.coroutines.CompletableDeferred

/** Raw core-network answer. Internal so core-network types never leak out of this layer. */
internal data class ProfilerResponse(
    val result: AuthenticationProfilerResult?,
    val error: SmartError?,
)

/** Bridge to core-network. Returns null when core-network finished without reporting anything. */
internal fun interface ProfilerGateway {
    suspend fun authenticate(serialNumber: String, params: ProfilerAuthenticationParams): ProfilerResponse?
}

/** [ProfilerGateway] backed by [SmartCore.authenticationProfiler]. */
internal class SmartCoreProfilerGateway : ProfilerGateway {

    override suspend fun authenticate(
        serialNumber: String,
        params: ProfilerAuthenticationParams,
    ): ProfilerResponse? {
        // SmartCore reports through a listener, invoked before authenticationProfiler() returns.
        // Keep only the first report in case it is invoked more than once.
        val response = CompletableDeferred<ProfilerResponse>()

        SmartCore.authenticationProfiler(params.toCore(serialNumber)) { result, error ->
            response.complete(ProfilerResponse(result, error))
        }

        return if (response.isCompleted) response.await() else null
    }

    private fun ProfilerAuthenticationParams.toCore(serialNumber: String) = AuthenticationProfilerParam(
        serialNumber = serialNumber,
        branchKey = branchKey.orEmpty(),
        smartKey = smartKey.orEmpty(),
        isThirdPartyFlow = isThirdPartyFlow,
        onboardingId = onboardingId,
    )
}

internal fun toSdkResult(
    result: AuthenticationProfilerResult?,
    error: SmartError?,
): ProfilerAuthenticationResult = when {
    error != null -> ProfilerAuthenticationResult.Error(error.errorCode, error.message)

    result == null -> ProfilerAuthenticationResult.Error(
        code = ProfilerAuthenticationResult.ErrorCodes.NO_RESPONSE,
        message = "Profiler authentication returned no data",
    )

    result.boardingOptions.isNotEmpty() -> ProfilerAuthenticationResult.BoardingSelectionRequired(
        options = result.boardingOptions.map {
            BoardingOption(id = it.onboardingId, name = it.onboardingName, description = it.onboardingDescription)
        },
    )

    else -> ProfilerAuthenticationResult.Success(token = result.token)
}
