package com.smart.smartpay.features.tip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.smart.smartpay.navigation.TipRoute
import com.smart.smartpay.domain.tip.GetTipConfigurationUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Tip chosen by the customer. */
sealed interface TipSelection {
    data object None : TipSelection
    data class Percentage(val percentage: Int) : TipSelection
    data class Custom(val amountInCents: Long) : TipSelection
}

data class TipUiState(
    val amountInCents: Long,
    val tipPercentages: List<Int> = emptyList(),
    val customTipEnabled: Boolean = false,
    /** Max free tip in cents, or null if there is no limit. */
    val customTipMaxInCents: Long? = null,
    val selection: TipSelection = TipSelection.None,
    val isLoading: Boolean = true,
) {
    val tipInCents: Long
        get() = when (selection) {
            TipSelection.None -> 0L
            is TipSelection.Percentage -> calculateTip(amountInCents, selection.percentage)
            is TipSelection.Custom -> selection.amountInCents
        }

    val totalInCents: Long get() = amountInCents + tipInCents

    /** Whether any tip option is shown (besides the summary). */
    val hasTipOptions: Boolean get() = tipPercentages.isNotEmpty() || customTipEnabled
}

/** Whether a free tip of [amountInCents] is within [maxInCents] (null = no limit). */
fun isCustomTipAllowed(amountInCents: Long, maxInCents: Long?): Boolean =
    maxInCents == null || amountInCents <= maxInCents

/** Tip for [percentage] of [amountInCents], rounded half-up to the nearest cent. */
fun calculateTip(amountInCents: Long, percentage: Int): Long =
    (amountInCents * percentage + 50) / 100

@HiltViewModel
class TipViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    getTipConfiguration: GetTipConfigurationUseCase,
) : ViewModel() {

    private val amountInCents = savedStateHandle.toRoute<TipRoute>().amountInCents

    // Selection saved as primitives so it survives rotation / process death
    private val selectedPercentage = savedStateHandle.getStateFlow(KEY_PERCENTAGE, NO_SELECTION)
    private val customTipInCents = savedStateHandle.getStateFlow(KEY_CUSTOM_TIP, NO_SELECTION.toLong())

    val uiState: StateFlow<TipUiState> = combine(
        // Downloaded with the profiler authentication (tipProcessing): fixed for this screen
        flowOf(getTipConfiguration()),
        selectedPercentage,
        customTipInCents,
    ) { configuration, percentage, customTip ->
        val percentages = if (configuration.enabled) configuration.percentages else emptyList()
        val customTipEnabled = configuration.enabled && configuration.customTipEnabled
        val customTipMax = configuration.customTipMaxPercentage?.let { calculateTip(amountInCents, it) }
        TipUiState(
            amountInCents = amountInCents,
            tipPercentages = percentages,
            customTipEnabled = customTipEnabled,
            customTipMaxInCents = customTipMax,
            // Ignore a saved selection that the current configuration no longer allows
            selection = when {
                customTip > 0 && customTipEnabled && isCustomTipAllowed(customTip, customTipMax) ->
                    TipSelection.Custom(customTip)
                percentage in percentages -> TipSelection.Percentage(percentage)
                else -> TipSelection.None
            },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TipUiState(amountInCents = amountInCents),
    )

    fun onNoTipSelected() = select(percentage = NO_SELECTION, customTip = NO_SELECTION.toLong())

    fun onPercentageSelected(percentage: Int) = select(percentage = percentage, customTip = NO_SELECTION.toLong())

    /** A custom tip of 0 is treated as "no tip"; one above the configured max is ignored. */
    fun onCustomTipEntered(amountInCents: Long) {
        if (!isCustomTipAllowed(amountInCents, uiState.value.customTipMaxInCents)) return
        select(percentage = NO_SELECTION, customTip = amountInCents)
    }

    private fun select(percentage: Int, customTip: Long) {
        savedStateHandle[KEY_PERCENTAGE] = percentage
        savedStateHandle[KEY_CUSTOM_TIP] = customTip
    }

    companion object {
        private const val KEY_PERCENTAGE = "selected_percentage"
        private const val KEY_CUSTOM_TIP = "custom_tip_in_cents"
        private const val NO_SELECTION = 0
    }
}
