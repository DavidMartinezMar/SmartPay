package com.smart.smartpay.data.authentication

import com.smart.smartpay.data.models.ProfilerResponse
import com.smart.smartpay.data.models.ProfilerAuthenticationParams

/** Bridge to core-network. Returns null when core-network finished without reporting anything. */
internal fun interface ProfilerGateway {
    suspend fun authenticate(serialNumber: String, params: ProfilerAuthenticationParams): ProfilerResponse?
}
