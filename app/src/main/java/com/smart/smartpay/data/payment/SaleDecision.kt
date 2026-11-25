package com.smart.smartpay.data.payment

import com.smart.smartpay.core.PaymentSdk
import com.smart.smartpay.domain.payment.Decision
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration

/** [Decision] resolved once: [onResolved] gets the chosen value, or null if cancelled / timed out. */
internal class SaleDecision<T>(private val onResolved: (T?) -> Unit) : Decision<T> {
    private val resolved = AtomicBoolean(false)

    val isResolved: Boolean get() = resolved.get()

    override fun choose(value: T) {
        if (resolved.compareAndSet(false, true)) onResolved(value)
    }

    override fun cancel() {
        if (resolved.compareAndSet(false, true)) onResolved(null)
    }
}

/** Open decisions of a sale, cancelled together when the sale flow stops. */
internal class DecisionTracker {
    private val open = CopyOnWriteArrayList<SaleDecision<*>>()

    fun add(decision: SaleDecision<*>) {
        open += decision
    }

    fun remove(decision: SaleDecision<*>) {
        open -= decision
    }

    fun cancelAll() {
        open.forEach { it.cancel() }
        open.clear()
    }
}

/**
 * Decision answered through an application event (doesn't block the SDK thread), used for the
 * card reading questions (application selection, card digits) of every card transaction.
 * Cancelled automatically after [timeout].
 */
internal fun <T> asyncDecision(
    timeout: Duration,
    decisions: DecisionTracker,
    onChoose: suspend (T) -> Unit,
    onCancel: suspend () -> Unit,
): Decision<T> {
    lateinit var decision: SaleDecision<T>
    val timeoutJob = PaymentSdk.scope.launch {
        delay(timeout)
        decision.cancel()
    }
    decision = SaleDecision { value ->
        timeoutJob.cancel()
        decisions.remove(decision)
        PaymentSdk.scope.launch { if (value != null) onChoose(value) else onCancel() }
    }
    decisions.add(decision)
    return decision
}
