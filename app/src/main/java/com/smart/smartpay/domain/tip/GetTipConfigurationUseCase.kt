package com.smart.smartpay.domain.tip

import com.smart.core.business.remote.response.profilerParameters.parameters.novusParameters.transactional.TransactionalParameterTipProcessingValues
import com.smart.core.business.remote.response.profilerParameters.parameters.novusParameters.transactional.TransactionalParameters
import com.smart.smartpay.core.TerminalSession

/**
 * Tip configuration downloaded with the last successful profiler authentication
 * ([com.smart.smartpay.domain.authentication.AuthenticateProfilerUseCase]).
 *
 * Returns [TipConfiguration.DISABLED] if the terminal is not authenticated yet or the profiler
 * sent no `tipProcessing` parameter.
 */
class GetTipConfigurationUseCase internal constructor(
    private val transactionalParameters: () -> TransactionalParameters?,
) {

    constructor() : this(transactionalParameters = { TerminalSession.params?.transactionalParameters })

    operator fun invoke(): TipConfiguration =
        transactionalParameters()?.tipProcessing?.toTipConfiguration() ?: TipConfiguration.DISABLED
}

/** Same interpretation as SmartWrapper (common's TipConfigurationResponse / BaseTipFragment). */
internal fun TransactionalParameterTipProcessingValues.toTipConfiguration(): TipConfiguration {
    val customAmount = properties?.customAmount
    return TipConfiguration(
        enabled = enabled,
        percentages = value.filter { it > 0 }.distinct(),
        customTipEnabled = customAmount?.enable == true,
        // e.g. "50" or "50%". Unreadable means 100%, as SmartWrapper's validateTipMaxPercent()
        customTipMaxPercentage = customAmount?.maxPercentage
            ?.replace("%", "")
            ?.trim()
            ?.toDoubleOrNull()
            ?.toInt()
            ?: MAX_PERCENTAGE,
    )
}

private const val MAX_PERCENTAGE = 100
