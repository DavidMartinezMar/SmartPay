package com.smart.smartpay.data.models

import com.smart.smartpay.domain.models.Decision
import kotlinx.coroutines.CompletableDeferred

/** Adapts a [CompletableDeferred] decision (core-emv-sale) to the domain's [Decision]. */
fun <Public, Internal> deferredDecision(
    deferred: CompletableDeferred<Internal>,
    map: (Public) -> Internal,
): Decision<Public> = object : Decision<Public> {
    override fun choose(value: Public) {
        deferred.complete(map(value))
    }

    override fun cancel() {
        deferred.cancel()
    }
}