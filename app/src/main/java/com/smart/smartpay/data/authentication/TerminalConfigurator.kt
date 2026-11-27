package com.smart.smartpay.data.authentication

import com.smart.core.business.model.AuthenticationProfilerResult
import com.smart.smartpay.domain.models.ProfilerAuthenticationResult

/** Leaves the terminal ready to sell after a successful profiler authentication. */
internal fun interface TerminalConfigurator {
    /** Returns null on success, or the error that prevented the setup. */
    suspend fun configure(result: AuthenticationProfilerResult): ProfilerAuthenticationResult.Error?
}
