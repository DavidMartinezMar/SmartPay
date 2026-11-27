package com.smart.smartpay.data.models

import java.util.concurrent.CopyOnWriteArrayList

/** Open decisions of a sale, cancelled together when the sale flow stops. */
class DecisionTracker {
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