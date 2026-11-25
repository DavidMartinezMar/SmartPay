package com.smart.smartpay.core

import com.smart.core.business.model.profiler.SacNovusParams
import com.smart.core_emv.di.CoreEmvModule
import com.smart.core_emv.domain.models.EmvParams
import com.smart.core_payments.domain.model.ContactlessLedState
import com.smart.core_payments.domain.repository.LedController
import com.smart.core_payments.domain.repository.SerialConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.time.Duration

/**
 * Serial link is only used by core-payments for external pinpads (DLLTerminalClient, off-line
 * authentication flavor). Integrated terminals talk to the host over SSL, so it is never called.
 */
internal object NoOpSerialConnection : SerialConnection {
    override suspend fun write(data: ByteArray): Result<Boolean> = Result.success(false)
    override suspend fun writeFlat(data: ByteArray): Result<Boolean> = Result.success(false)
    override suspend fun read(timeoutMs: Duration): Flow<ByteArray> = emptyFlow()
    override fun abortRead() = Unit
}

/**
 * No-op temporarily: common's SmartDeviceManager (LED control) is no longer a dependency.
 * TODO: restore once a core-emv/core-network replacement exists.
 */
internal object DeviceLedController : LedController {
    override fun setContactlessLedState(state: ContactlessLedState) = Unit
    override fun setAllReadingLed(isOn: Boolean) = Unit
}

/**
 * Built once from in-memory state (no disk persistence: re-authenticates on every process
 * start). Registered with [CoreEmvModule.init] from [PaymentSdk.initialize].
 */
internal fun buildEmvParams(): EmvParams = EmvParams(
    context = PaymentSdk.applicationContext,
    smartJPayment = CoreEmvModule.smartJPayment,
    paramConfig = TerminalSession.params ?: EMPTY_SAC_NOVUS_PARAMS,
    sdkProviderService = null,
    serialNumber = TerminalSession.serialNumber.orEmpty(),
    cachedToken = TerminalSession.cachedToken,
    cachedAcquirersJson = TerminalSession.cachedAcquirersJson,
)

/** Never actually used: buildEmvParams() only runs (lazily) after a successful authentication. */
private val EMPTY_SAC_NOVUS_PARAMS = SacNovusParams(
    keyBradesco = null,
    transactionalParameters = null,
    systemConfigurationParameters = null,
    userInterfaceParameters = null,
    documentParameters = null,
)
