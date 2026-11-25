package com.smart.smartpay.data.payment

import com.smart.core.business.model.profiler.SacNovusParams
import com.smart.core_emv.utils.EntryModeFlag
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Terminal-derived parameters shared by every card transaction (sale and operation):
 * ported from SmartWrapper so the payment flows don't depend on a merchant UI layer for them.
 */

private fun Int?.toEnabledBoolean(): Boolean = this == 1

/** Chip always; swipe and contactless as the merchant's transactional parameters allow. */
internal fun entryModeFlag(terminalParams: SacNovusParams?): Int {
    val transactional = terminalParams?.transactionalParameters
    return EntryModeFlag.CHIP or
        (if (transactional?.magneticStripeTransactionEnabled?.value.toEnabledBoolean()) EntryModeFlag.MAGNETIC else 0) or
        (if (transactional?.contactlessTransactionEnabled?.value.toEnabledBoolean()) EntryModeFlag.CONTACTLESS else 0)
}

/** How long a customer decision may stay unanswered (terminal's data entry timeout). */
internal fun decisionTimeout(terminalParams: SacNovusParams?): Duration =
    terminalParams?.userInterfaceParameters?.screenDataEntryTimeout?.value
        ?.toLongOrNull()?.takeIf { it > 0 }?.seconds
        ?: DEFAULT_DECISION_TIMEOUT

internal val DEFAULT_DECISION_TIMEOUT: Duration = 30.seconds
