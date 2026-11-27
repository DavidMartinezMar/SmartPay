package com.smart.smartpay.data.authentication

import android.util.Log
import com.google.gson.Gson
import com.smart.core.business.model.AuthenticationProfilerResult
import com.smart.core_emv.di.CoreEmvModule
import com.smart.core_emv.domain.models.EmvSettings
import com.smart.core_emv.domain.usecases.InitEmvUseCase
import com.smart.smartpay.core.TerminalSessionManager
import com.smart.smartpay.domain.models.ErrorCodes
import com.smart.smartpay.domain.models.ProfilerAuthenticationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.minutes

/**
 * Initializes the EMV module.
 *
 * Kept only in memory (TerminalSessionManager): re-authenticates on every process start, so there is
 * nothing to persist to disk.
 */
internal class SmartCoreTerminalConfigurator : TerminalConfigurator {

    private companion object {
        val EMV_INIT_TIMEOUT = 1.minutes
    }

    override suspend fun configure(result: AuthenticationProfilerResult): ProfilerAuthenticationResult.Error? {
        val params = result.params ?: return setupError("The profiler returned no terminal parameters")
        return try {
            TerminalSessionManager.params = params
            TerminalSessionManager.cachedToken = result.token
            TerminalSessionManager.cachedAcquirersJson = Gson().toJson(result.acquirers)
            CoreEmvModule.paymentParamsProvider.update(
                sys = params.systemConfigurationParameters,
                doc = params.documentParameters,
                ui = params.userInterfaceParameters,
                trx = params.transactionalParameters,
                key = params.keyBradesco,
            )

            // SET_SETTINGS + INITIALIZE (loads kernels on first run)
            val emvReady = withTimeoutOrNull(EMV_INIT_TIMEOUT) {
                CoreEmvModule.getUseCase(InitEmvUseCase::class.java)(EmvSettings(requireDigitalNip = false)).first()
            }

            Log.d("SMARTPAY", "SDK EMV - Inicializado -> $emvReady")
            if (emvReady != true) return setupError("The EMV module could not be initialized")
            TerminalSessionManager.isReadyForSale = true
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setupError("Terminal setup failed: ${e.message}")
        }
    }

    private fun setupError(message: String) = ProfilerAuthenticationResult.Error(
        code = ErrorCodes.TERMINAL_SETUP_FAILED,
        message = message,
    )
}
