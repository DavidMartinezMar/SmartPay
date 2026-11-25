package com.smart.smartpay.core

import com.smart.core.business.model.profiler.SacNovusParams

/** In-memory state of the terminal for the current process. */
internal object TerminalSession {

    /** Profiler parameters of the last successful authentication. */
    @Volatile
    var params: SacNovusParams? = null

    /** Read once during authentication; reused by the sale flow (no need to re-read the device). */
    @Volatile
    var serialNumber: String? = null

    /**
     * Profiler token and acquirers of the last successful authentication, kept in memory only
     * (re-authenticates on every process start, so there is nothing to persist to disk).
     * Feeds CoreEmvModule's EmvParams without a round trip through storage.
     */
    @Volatile
    var cachedToken: String = ""

    @Volatile
    var cachedAcquirersJson: String = ""

    /** True once the terminal was authenticated and the EMV module initialized. */
    @Volatile
    var isReadyForSale: Boolean = false

    /**
     * Set when a sale ends asking to retry with chip (contactless failed): the next sale
     * must disable contactless (core-payments CTLS_ERR_RETRY), as SmartWrapper does.
     */
    @Volatile
    var disableContactlessOnNextSale: Boolean = false
}
