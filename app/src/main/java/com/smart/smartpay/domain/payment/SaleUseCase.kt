package com.smart.smartpay.domain.payment

import com.smart.smartpay.core.PaymentSdkManager
import com.smart.smartpay.core.TerminalSessionManager
import com.smart.smartpay.core.TransactionLock
import com.smart.smartpay.data.payment.SaleEngine
import com.smart.smartpay.data.payment.SmartJPaymentSaleEngine
import com.smart.smartpay.domain.models.SaleErrorCodes.NOT_INITIALIZED
import com.smart.smartpay.domain.models.SaleErrorCodes.SALE_IN_PROGRESS
import com.smart.smartpay.domain.models.SaleErrorCodes.TERMINAL_NOT_READY
import com.smart.smartpay.domain.models.SaleRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Card sale (chip, swipe, contactless or manual entry) with PIN, promotions, DCC and USD.
 *
 * ```
 * SaleUseCase()(SaleRequest(amountInCents = 12550, tipInCents = 1500)).collect { event ->
 *     when (event) {
 *         SaleEvent.WaitingForCard -> showInsertCard()
 *         SaleEvent.PinRequested -> showEnterPinOnTerminal()
 *         is SaleEvent.PromotionsOffered -> askCustomer(event.options) { event.decision.choose(it) }
 *         is SaleEvent.Approved -> showReceipt(event.receipt)
 *         is SaleEvent.Declined -> showDeclined(event.errorMessage)
 *         else -> updateStatus(event)
 *     }
 * }
 * ```
 *
 * The terminal must be ready: [com.smart.smartpay.domain.authentication.AuthenticateProfilerUseCase]
 * succeeded in this process. One sale at a time. The PIN is always entered on the terminal's own
 * secure PIN screen (the app doesn't render its own PIN pad for a sale).
 *
 * Stop collecting only after the result: once the card was read the transaction can't be
 * cancelled (use [cancel] while waiting for the card), and a result that arrives after you stop
 * collecting is lost. Sales are not stored here (see the transactions history).
 */
class SaleUseCase internal constructor(
    private val engine: () -> SaleEngine,
    private val isSdkInitialized: () -> Boolean,
    private val isTerminalReady: () -> Boolean,
) {

    constructor() : this(
        engine = { SmartJPaymentSaleEngine() },
        isSdkInitialized = { PaymentSdkManager.isInitialized },
        isTerminalReady = { TerminalSessionManager.isReadyForSale },
    )

    operator fun invoke(request: SaleRequest): Flow<SaleEvent> = flow {
        when {
            isSdkInitialized().not() -> emit(failed(NOT_INITIALIZED, "Call PaymentSdkManager.initialize(application) first"))
            isTerminalReady().not() -> emit(failed(TERMINAL_NOT_READY, "Authenticate the terminal first"))
            TransactionLock.tryAcquire().not() -> emit(failed(SALE_IN_PROGRESS, "Another transaction is in progress"))
            else -> try {
                emitAll(engine().sale(request))
            } finally {
                TransactionLock.release()
            }
        }
    }

    /** Cancels the sale while it waits for the card. The flow then ends with [SaleEvent.Declined]. */
    suspend fun cancel() {
        if (isSdkInitialized()) engine().cancelCardRead()
    }

    private fun failed(code: String, message: String) = SaleEvent.Failed(code, message)
}
