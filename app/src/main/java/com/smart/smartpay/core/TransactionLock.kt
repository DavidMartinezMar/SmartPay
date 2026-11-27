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
