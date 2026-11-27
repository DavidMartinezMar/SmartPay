package com.smart.smartpay.data.models

import com.smart.core.business.model.AuthenticationProfilerResult
import com.smart.core.core.model.SmartError

/** Raw core-network answer. Internal so core-network types never leak out of this layer. */
internal data class ProfilerResponse(
    val result: AuthenticationProfilerResult?,
    val error: SmartError?,
)