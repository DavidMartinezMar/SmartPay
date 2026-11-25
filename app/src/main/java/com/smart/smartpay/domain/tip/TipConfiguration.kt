package com.smart.smartpay.domain.tip

/**
 * Tip configuration of the terminal, downloaded with the profiler authentication
 * (`transactionalParameters.tipProcessing`).
 *
 * @property enabled whether the tip step is offered at all.
 * @property percentages tip options offered to the customer (e.g. 10, 15, 20).
 * @property customTipEnabled whether the customer can enter a free tip amount.
 * @property customTipMaxPercentage max free tip, as a percentage of the sale amount
 * (e.g. 50 = up to half the sale). Null means no limit.
 */
data class TipConfiguration(
    val enabled: Boolean,
    val percentages: List<Int>,
    val customTipEnabled: Boolean,
    val customTipMaxPercentage: Int?,
) {
    /** Whether the customer is asked for a tip: enabled and with at least one option to choose. */
    val isTipOffered: Boolean
        get() = enabled && (percentages.isNotEmpty() || customTipEnabled)

    companion object {
        /** Used when the terminal has no tip configuration (not authenticated, or no `tipProcessing`). */
        val DISABLED: TipConfiguration = TipConfiguration(
            enabled = false,
            percentages = emptyList(),
            customTipEnabled = false,
            customTipMaxPercentage = null,
        )
    }
}
