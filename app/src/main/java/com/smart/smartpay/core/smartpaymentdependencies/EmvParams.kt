package com.smart.smartpay.core.smartpaymentdependencies

import com.smart.core.business.model.profiler.SacNovusParams
import com.smart.core_emv.di.CoreEmvModule
import com.smart.core_emv.domain.models.EmvParams
import com.smart.smartpay.core.PaymentSdkManager
import com.smart.smartpay.core.TerminalSessionManager

object EmvParams {
    /**
     * Never actually used: buildEmvParams() only runs (lazily) after a successful authentication.
     */
    private val EMPTY_SAC_NOVUS_PARAMS = SacNovusParams(
        keyBradesco = null,
        transactionalParameters = null,
        systemConfigurationParameters = null,
        userInterfaceParameters = null,
        documentParameters = null,
    )

    /**
     * Built once from in-memory state (no disk persistence: re-authenticates on every process
     * start). Registered with [CoreEmvModule.init] from [PaymentSdkManager.initialize].
     */
    fun buildEmvParams(): EmvParams = EmvParams(
        context = PaymentSdkManager.applicationContext,
        smartJPayment = CoreEmvModule.smartJPayment,
        paramConfig = TerminalSessionManager.params ?: EMPTY_SAC_NOVUS_PARAMS,
        sdkProviderService = null, // Only provide it for Ingenico models
        serialNumber = TerminalSessionManager.serialNumber.orEmpty(),
        cachedToken = TerminalSessionManager.cachedToken,
        cachedAcquirersJson = TerminalSessionManager.cachedAcquirersJson,
    )
}