package com.smart.smartpay.domain.payment

/**
 * A question the sale is waiting on (e.g. which promotion the customer wants).
 *
 * Answer with [choose] or [cancel], once; later calls are ignored. If it is not answered within
 * the terminal's data entry timeout (or the sale flow stops being collected) it is cancelled,
 * so the card reader is never left waiting.
 */
interface Decision<T> {
    fun choose(value: T)
    fun cancel()
}
