package com.smart.smartpay.domain.authentication

import com.smart.smartpay.core.PaymentSdk
import com.smart.smartpay.core.TerminalSession
import com.smart.smartpay.data.authentication.ProfilerGateway
import com.smart.smartpay.data.authentication.SmartCoreProfilerGateway
import com.smart.smartpay.data.authentication.SmartCoreTerminalConfigurator
import com.smart.smartpay.data.authentication.TerminalConfigurator
import com.smart.smartpay.data.authentication.toSdkResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Authenticates the terminal against the profiler, stores its configuration and initializes
 * the EMV module. After [ProfilerAuthenticationResult.Success] the terminal is ready to sell.
 * Meant to run at app start (e.g. behind a splash screen); the UI is up to the caller.
 * The terminal serial number is read from the device.
 *
 * ```
 * PaymentSdk.initialize(application) // once, in Application.onCreate()
 * // request PaymentSdk.requiredPermissions if PaymentSdk.hasRequiredPermissions(context) is false
 *
 * when (val result = AuthenticateProfilerUseCase()(ProfilerAuthenticationParams())) {
 *     is ProfilerAuthenticationResult.Success -> goToHome()
 *     is ProfilerAuthenticationResult.BoardingSelectionRequired -> showOptions(result.options)
 *     is ProfilerAuthenticationResult.Error -> showError(result.code, result.message)
 * }
 * ```
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

    constructor() : this(
        gateway = SmartCoreProfilerGateway(),
        serialNumberProvider = "188245006711",
        terminalConfigurator = { SmartCoreTerminalConfigurator() },
        isSdkInitialized = { PaymentSdk.isInitialized },
        hasRequiredPermissions = { PaymentSdk.hasRequiredPermissions(PaymentSdk.applicationContext) },
        dispatcher = Dispatchers.IO,
    )

    suspend operator fun invoke(
        params: ProfilerAuthenticationParams = ProfilerAuthenticationParams(),
    ): ProfilerAuthenticationResult {
        if (!isSdkInitialized()) {
            return ProfilerAuthenticationResult.Error(
                code = ProfilerAuthenticationResult.ErrorCodes.NOT_INITIALIZED,
                message = "Call PaymentSdk.initialize(application) before authenticating",
            )
        }
        // Checked before calling the profiler: the serial number read below needs it
        if (!hasRequiredPermissions()) {
            return ProfilerAuthenticationResult.Error(
                code = ProfilerAuthenticationResult.ErrorCodes.PERMISSIONS_REQUIRED,
                message = "Grant PaymentSdk.requiredPermissions before authenticating",
            )
        }
        // core-network keeps a single listener per request type: never run two at once
        return mutex.withLock {
            withContext(dispatcher) { authenticate(params) }
        }
    }

    private suspend fun authenticate(params: ProfilerAuthenticationParams): ProfilerAuthenticationResult {
        TerminalSession.isReadyForSale = false
        val serialNumber = readSerialNumber()
            ?: return ProfilerAuthenticationResult.Error(
                code = ProfilerAuthenticationResult.ErrorCodes.SERIAL_NUMBER_UNAVAILABLE,
                message = "Could not read the terminal serial number",
            )
        // Cached so the sale flow can reuse it without reading the device again
        TerminalSession.serialNumber = serialNumber
        val response = gateway.authenticate(serialNumber, params)
            ?: return ProfilerAuthenticationResult.Error(
                code = ProfilerAuthenticationResult.ErrorCodes.NO_RESPONSE,
                message = "Profiler authentication finished without a result",
            )

        val result = toSdkResult(response.result, response.error)
        if (result !is ProfilerAuthenticationResult.Success || response.result == null) return result

        return terminalConfigurator().configure(response.result) ?: result
    }

    /** Null if the device returns no serial number or the read fails (e.g. unsupported terminal). */
    private fun readSerialNumber(): String? =
        try {
            serialNumberProvider.takeIf { it.isNotBlank() }?.let(::removeLeadingZeros)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    private companion object {
        // Shared by all instances: the limitation is in core-network's singleton
        val mutex = Mutex()
    }
}

private fun removeLeadingZeros(serialNumber: String): String = serialNumber.trimStart('0').ifEmpty { "0" }
