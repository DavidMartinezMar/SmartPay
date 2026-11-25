package com.smart.smartpay.core

import java.util.concurrent.atomic.AtomicBoolean

/**
 * core-payments runs one transaction at a time (sale, cancellation or refund): every use case
 * takes this lock before submitting to SmartJPayment.
 */
internal object TransactionLock {
    private val inProgress = AtomicBoolean(false)

    /** False if another transaction holds the lock. */
    fun tryAcquire(): Boolean = inProgress.compareAndSet(false, true)

    fun release() {
        inProgress.set(false)
    }
}

// Commented out temporarily: depended on the common module (device/account persistence).
// TODO: restore once cancellation/refund are migrated off common.
//
// /** Terminal capabilities that change how a card transaction is shown. */
// internal object TerminalUi {
//
//     /** Terminals where the PIN is typed on the app screen (SmartWrapper's BaseNipCardFragment). */
//     private val PIN_ON_APP_SCREEN_MODELS = setOf(
//         TerminalModel.CA_S2_PRO,
//         TerminalModel.CA_S1U2_M4,
//         TerminalModel.CA_S1U2,
//         TerminalModel.CA_S1F4,
//         TerminalModel.CA_S1F4_PRO,
//         TerminalModel.CA_S1E4_PRO,
//     )
//
//     val pinOnAppScreen: Boolean get() = getTerminalModel() in PIN_ON_APP_SCREEN_MODELS
// }
