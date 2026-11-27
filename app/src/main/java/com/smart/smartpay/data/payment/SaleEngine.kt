package com.smart.smartpay.data.payment

import com.smart.smartpay.domain.payment.SaleEvent
import com.smart.smartpay.domain.models.SaleRequest
import kotlinx.coroutines.flow.Flow
import com.smart.core_emv_sale.domain.usecases.SaleUseCase as CoreSaleUseCase

/** Runs a sale by consuming core-emv-sale's [CoreSaleUseCase] (SmartWrapper's own sale engine). */
internal interface SaleEngine {
    fun sale(request: SaleRequest): Flow<SaleEvent>

    /** Cancels while waiting for the card. */
    suspend fun cancelCardRead()
}
