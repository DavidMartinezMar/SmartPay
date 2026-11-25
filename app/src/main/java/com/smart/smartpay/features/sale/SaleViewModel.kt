package com.smart.smartpay.features.sale

import androidx.lifecycle.ViewModel
import com.smart.smartpay.domain.tip.GetTipConfigurationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SaleViewModel @Inject constructor(
    private val getTipConfiguration: GetTipConfigurationUseCase,
) : ViewModel() {

    /** Read on each charge: the tip configuration comes from the last profiler authentication. */
    fun isTipOffered(): Boolean = getTipConfiguration().isTipOffered
}
