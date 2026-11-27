package com.smart.smartpay.domain.utils

import com.smart.core.business.model.AuthenticationProfilerResult
import com.smart.core.core.model.SmartError
import com.smart.smartpay.domain.models.ErrorCodes
import com.smart.smartpay.domain.models.ProfilerAuthenticationResult

fun toSdkResult(
    result: AuthenticationProfilerResult?,
    error: SmartError?,
): ProfilerAuthenticationResult = when {
    error != null -> ProfilerAuthenticationResult.Error(error.errorCode, error.message)

    result == null -> ProfilerAuthenticationResult.Error(
        code = ErrorCodes.NO_RESPONSE,
        message = "Profiler authentication returned no data",
    )

    else -> ProfilerAuthenticationResult.Success(token = result.token)
}

/** Null if the device returns no serial number or the read fails (e.g. unsupported terminal). */
fun readSerialNumber(serialNumber: String): String? = serialNumber.takeIf { it.isNotBlank() }