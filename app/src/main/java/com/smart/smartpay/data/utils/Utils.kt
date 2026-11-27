package com.smart.smartpay.data.utils

import com.smart.core.business.model.profiler.SacNovusParams
import com.smart.core_emv.domain.models.FlapDynamicFields
import com.smart.core_emv.utils.EntryModeFlag
import com.smart.core_emv_sale.data.models.EmvSaleParams
import com.smart.smartpay.core.PaymentSdkManager
import com.smart.smartpay.core.TerminalSessionManager
import com.smart.smartpay.data.models.DecisionTracker
import com.smart.smartpay.data.models.SaleDecision
import com.smart.smartpay.data.utils.extensionfunctions.toEnabledBoolean
import com.smart.smartpay.domain.models.SaleReceipt
import com.smart.smartpay.domain.models.Decision
import com.smart.smartpay.domain.models.SaleCurrency
import com.smart.smartpay.domain.models.SaleRequest
import com.smart.smartpay.utils.ENABLED_USD
import com.smart.smartpay.utils.TRANSACTION_TYPE_SALE
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.text.orEmpty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Decision answered through an application event (doesn't block the SDK thread), used for the
 * card reading questions (application selection, card digits) of every card transaction.
 * Cancelled automatically after [timeout].
 */
fun <T> asyncDecision(
    timeout: Duration,
    decisions: DecisionTracker,
    onChoose: suspend (T) -> Unit,
    onCancel: suspend () -> Unit,
): Decision<T> {
    lateinit var decision: SaleDecision<T>
    val timeoutJob = PaymentSdkManager.scope.launch {
        delay(timeout)
        decision.cancel()
    }
    decision = SaleDecision { value ->
        timeoutJob.cancel()
        decisions.remove(decision)
        PaymentSdkManager.scope.launch { if (value != null) onChoose(value) else onCancel() }
    }
    decisions.add(decision)
    return decision
}

fun buildEmvSaleParams(request: SaleRequest, terminalParams: SacNovusParams?): EmvSaleParams {
    val usd = terminalParams?.systemConfigurationParameters?.usdTransactionControl
    val manualCard = request.manualCard
    return EmvSaleParams(
        amount = request.amountInCents.toString(),
        // Overwritten by CoreSaleUseCase itself (it generates its own tap map / digit order)
        pinTapMap = "",
        pinNumbersMap = emptyList(),
        // Blank: core-emv-sale generates one from the real serial number below
        txnUniqueId = request.reference.orEmpty(),
        currencyCode = request.currency.code,
        isCurrencyUsd = usd != null && usd.enabled && usd.value == ENABLED_USD,
        tip = request.tipInCents.toString(),
        cashierName = request.cashierName.orEmpty(),
        afiliation = "",
        transactionType = TRANSACTION_TYPE_SALE,
        disableCtls = TerminalSessionManager.disableContactlessOnNextSale
            .also { TerminalSessionManager.disableContactlessOnNextSale = false },
        iso = "",
        txnRestrictions = "",
        allowManualEntry = false,
        transactionTimeOut = "",
        flapDynamicFields = FlapDynamicFields(),
        entryModeFlag = entryModeFlag(terminalParams),
        cardNumber = manualCard?.cardNumber.orEmpty(),
        cardHolderName = manualCard?.cardHolderName.orEmpty(),
        expirationDate = manualCard?.expirationDate.orEmpty(),
        cvv = manualCard?.cvv.orEmpty(),
        // Only authenticates through the profiler's SAC Novus path (see ProfilerGateway)
        isAuthenticationSacNovus = true,
        // Cached by AuthenticateProfilerUseCase; the sale flow doesn't read the device itself
        serialNumber = TerminalSessionManager.serialNumber.orEmpty(),
    )
}

fun entryModeFlag(terminalParams: SacNovusParams?): Int {
    val transactional = terminalParams?.transactionalParameters
    return EntryModeFlag.CHIP or
            (if (transactional?.magneticStripeTransactionEnabled?.value.toEnabledBoolean()) EntryModeFlag.MAGNETIC else 0) or
            (if (transactional?.contactlessTransactionEnabled?.value.toEnabledBoolean()) EntryModeFlag.CONTACTLESS else 0)
}

/** How long a customer decision may stay unanswered (terminal's data entry timeout). */
fun decisionTimeout(terminalParams: SacNovusParams?): Duration =
    terminalParams?.userInterfaceParameters?.screenDataEntryTimeout?.value
        ?.toLongOrNull()?.takeIf { it > 0 }?.seconds
        ?: 30.seconds

/** CURRENCY_CODE comes as "484" / "0484"; pesos if unknown. */
fun currencyOf(receipt: SaleReceipt): SaleCurrency =
    when (receipt.raw["CURRENCY_CODE"].orEmpty().trimStart('0')) {
        "840" -> SaleCurrency.USD
        else -> SaleCurrency.MXN
    }