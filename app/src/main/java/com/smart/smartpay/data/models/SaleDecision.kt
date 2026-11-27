package com.smart.smartpay.data.models

import com.smart.smartpay.domain.models.Decision
import java.util.concurrent.atomic.AtomicBoolean

/** [Decision] resolved once: [onResolved] gets the chosen value, or null if cancelled / timed out. */
class SaleDecision<T>(private val onResolved: (T?) -> Unit) : Decision<T> {
    private val resolved = AtomicBoolean(false)

    override fun choose(value: T) {
        if (resolved.compareAndSet(false, true)) onResolved(value)
    }

    override fun cancel() {
        if (resolved.compareAndSet(false, true)) onResolved(null)
    }
}
