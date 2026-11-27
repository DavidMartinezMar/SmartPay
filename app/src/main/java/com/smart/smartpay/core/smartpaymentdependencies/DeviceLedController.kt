package com.smart.smartpay.core.smartpaymentdependencies

import com.smart.core_payments.domain.model.ContactlessLedState
import com.smart.core_payments.domain.repository.LedController

/**
 * No-op Create model only for a dependency.
 */
internal object DeviceLedController : LedController {
    override fun setContactlessLedState(state: ContactlessLedState) = Unit
    override fun setAllReadingLed(isOn: Boolean) = Unit
}