package com.smart.smartpay.data.authentication

import com.google.gson.Gson
import com.smart.core.business.model.AuthenticationProfilerResult
import com.smart.core_emv.di.CoreEmvModule
import com.smart.core_emv.domain.models.EmvSettings
import com.smart.core_emv.domain.usecases.InitEmvUseCase
import com.smart.smartpay.core.TerminalSession
import com.smart.smartpay.domain.authentication.ProfilerAuthenticationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.minutes

/** Leaves the terminal ready to sell after a successful profiler authentication. */
internal fun interface TerminalConfigurator {
    /** Returns null on success, or the error that prevented the setup. */
    suspend fun configure(result: AuthenticationProfilerResult): ProfilerAuthenticationResult.Error?
}

/**
 * Does what SmartWrapper's app does after the profiler (AuthenticationManager /
 * FlowAuthenticationHelper / FillConfigurationFile / ParamsViewModel / SplashFragment.initSdk):
 * keeps the terminal parameters, token and acquirers, then initializes the EMV module.
 *
 * Kept only in memory (TerminalSession): re-authenticates on every process start, so there is
 * nothing to persist to disk, unlike SmartWrapper's own app.
 */
internal class SmartCoreTerminalConfigurator : TerminalConfigurator {

    override suspend fun configure(result: AuthenticationProfilerResult): ProfilerAuthenticationResult.Error? {
        val params = result.params ?: return setupError("The profiler returned no terminal parameters")
        return try {
            TerminalSession.params = params
            TerminalSession.cachedToken = result.token
            TerminalSession.cachedAcquirersJson = Gson().toJson(result.acquirers)
            CoreEmvModule.paymentParamsProvider.update(
                sys = params.systemConfigurationParameters,
                doc = params.documentParameters,
                ui = params.userInterfaceParameters,
                trx = params.transactionalParameters,
                key = params.keyBradesco,
            )

            // SET_SETTINGS + INITIALIZE (loads kernels on first run: can take a while)
            val emvReady = withTimeoutOrNull(EMV_INIT_TIMEOUT) {
                CoreEmvModule.getUseCase(InitEmvUseCase::class.java)(EmvSettings(requireDigitalNip = true)).first()
            }
            if (emvReady != true) return setupError("The EMV module could not be initialized")

            TerminalSession.isReadyForSale = true
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setupError("Terminal setup failed: ${e.message}")
        }
    }

    private fun setupError(message: String) = ProfilerAuthenticationResult.Error(
        code = ProfilerAuthenticationResult.ErrorCodes.TERMINAL_SETUP_FAILED,
        message = message,
    )

    private companion object {
        val EMV_INIT_TIMEOUT = 2.minutes
    }
}
