package com.smart.smartpay.data.authentication

import com.smart.core.SmartCore
import com.smart.core.core.model.AuthenticationProfilerParam
import com.smart.smartpay.data.models.ProfilerResponse
import com.smart.smartpay.data.models.ProfilerAuthenticationParams
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

internal class ProfilerGatewayImpl : ProfilerGateway {
    override suspend fun authenticate(
        serialNumber: String,
        params: ProfilerAuthenticationParams,
    ): ProfilerResponse? {
        val response = CompletableDeferred<ProfilerResponse>()
        SmartCore.authenticationProfiler(params.toCore(serialNumber)) { result, error ->
            response.complete(ProfilerResponse(result, error))
        }
        return withTimeoutOrNull(20_000.milliseconds) { response.await() }
    }

    private fun ProfilerAuthenticationParams.toCore(serialNumber: String) = AuthenticationProfilerParam(
        serialNumber = serialNumber,
        branchKey = branchKey.orEmpty(),
        smartKey = smartKey.orEmpty(),
        isThirdPartyFlow = isThirdPartyFlow,
        onboardingId = onboardingId,
    )
}