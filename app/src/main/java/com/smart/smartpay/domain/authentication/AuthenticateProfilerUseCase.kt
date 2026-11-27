package com.smart.smartpay.domain.authentication

import com.smart.smartpay.core.PaymentSdkManager
import com.smart.smartpay.core.TerminalSessionManager
import com.smart.smartpay.data.authentication.ProfilerGateway
import com.smart.smartpay.data.authentication.ProfilerGatewayImpl
import com.smart.smartpay.data.authentication.SmartCoreTerminalConfigurator
import com.smart.smartpay.data.authentication.TerminalConfigurator
import com.smart.smartpay.data.models.ProfilerAuthenticationParams
import com.smart.smartpay.domain.models.ErrorCodes
import com.smart.smartpay.domain.models.ProfilerAuthenticationResult
import com.smart.smartpay.domain.utils.readSerialNumber
import com.smart.smartpay.domain.utils.toSdkResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Authenticates the terminal against the profiler, stores its configuration and initializes
 * the EMV module. After [com.smart.smartpay.domain.models.ProfilerAuthenticationResult.Success] the terminal is ready to sell.
 * Meant to run at app start (e.g. behind a splash screen); the UI is up to the caller.
 * The terminal serial number is read from the device.
 *
 * Main-safe: runs on a background dispatcher. Concurrent calls run one after another.
 */
class AuthenticateProfilerUseCase internal constructor(
    private val gateway: ProfilerGateway,
    private val serialNumberProvider: String,
    private val terminalConfigurator: () -> TerminalConfigurator,
    private val isSdkInitialized: () -> Boolean,
    private val hasRequiredPermissions: () -> Boolean,
    private val dispatcher: CoroutineDispatcher,
) {
    private companion object {
        val mutex = Mutex()
    }

    constructor() : this(
        gateway = ProfilerGatewayImpl(),
        serialNumberProvider = "188245006711",
        terminalConfigurator = { SmartCoreTerminalConfigurator() },
        isSdkInitialized = { PaymentSdkManager.isInitialized },
        hasRequiredPermissions = { PaymentSdkManager.hasRequiredPermissions(PaymentSdkManager.applicationContext) },
        dispatcher = Dispatchers.IO,
    )

    suspend operator fun invoke(
        params: ProfilerAuthenticationParams = ProfilerAuthenticationParams(),
    ): ProfilerAuthenticationResult {
        if (isSdkInitialized().not()) {
            return ProfilerAuthenticationResult.Error(
                code = ErrorCodes.NOT_INITIALIZED,
                message = "Call PaymentSdkManager.initialize(application) before authenticating",
            )
        }
        if (hasRequiredPermissions().not()) {
            return ProfilerAuthenticationResult.Error(
                code = ErrorCodes.PERMISSIONS_REQUIRED,
                message = "Grant PaymentSdkManager.requiredPermissions before authenticating",
            )
        }
        return mutex.withLock {
            withContext(dispatcher) { authenticate(params) }
        }
    }

    private suspend fun authenticate(params: ProfilerAuthenticationParams): ProfilerAuthenticationResult {
        TerminalSessionManager.isReadyForSale = false
        val serialNumber = readSerialNumber(serialNumberProvider).orEmpty()
        TerminalSessionManager.serialNumber = serialNumber
        val response = gateway.authenticate(serialNumber, params)
            ?: return ProfilerAuthenticationResult.Error(
                code = ErrorCodes.NO_RESPONSE,
                message = "Profiler authentication finished without a result",
            )

        val result = toSdkResult(response.result, response.error)
        if (result !is ProfilerAuthenticationResult.Success || response.result == null) return result

        return terminalConfigurator().configure(response.result) ?: result
    }
}
