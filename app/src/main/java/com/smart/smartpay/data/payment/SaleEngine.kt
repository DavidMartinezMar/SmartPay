package com.smart.smartpay.data.payment

import com.smart.core.business.model.profiler.SacNovusParams
import com.smart.core_emv.di.CoreEmvModule
import com.smart.core_emv.domain.models.FlapDynamicFields
import com.smart.core_emv.domain.usecases.CancelByUserUseCase
import com.smart.core_emv.domain.usecases.CancelInsertCardUseCase
import com.smart.core_emv.domain.usecases.SendApplicationEventUseCase
import com.smart.core_emv.domain.usecases.SetCvvRequiredUseCase
import com.smart.core_emv.domain.usecases.SetLastDigitsRequiredUseCase
import com.smart.core_emv_sale.data.models.EmvSaleParams
import com.smart.core_emv_sale.di.CoreEmvSaleModule
import com.smart.core_emv_sale.domain.models.DccOfferAvailable
import com.smart.core_emv_sale.domain.models.DccOfferSelected
import com.smart.core_emv_sale.domain.models.IntercardData
import com.smart.core_emv_sale.domain.models.PromotionSelected
import com.smart.core_emv_sale.domain.models.PromotionsAvailable
import com.smart.core_emv_sale.domain.models.SaleTransactionResult
import com.smart.core_emv_sale.domain.usecases.SaleUseCase as CoreSaleUseCase
import com.smart.smartpay.core.TerminalSession
import com.smart.smartpay.domain.payment.CardApplication
import com.smart.smartpay.domain.payment.CardDigitsType
import com.smart.smartpay.domain.payment.DccChoice
import com.smart.smartpay.domain.payment.DccOffer
import com.smart.smartpay.domain.payment.Decision
import com.smart.smartpay.domain.payment.Intercard
import com.smart.smartpay.domain.payment.PromotionChoice
import com.smart.smartpay.domain.payment.PromotionOptions
import com.smart.smartpay.domain.payment.SaleEvent
import com.smart.smartpay.domain.payment.SaleRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Runs a sale by consuming core-emv-sale's [CoreSaleUseCase] (SmartWrapper's own sale engine). */
internal interface SaleEngine {
    fun sale(request: SaleRequest): Flow<SaleEvent>

    /** Cancels while waiting for the card. */
    suspend fun cancelCardRead()
}

/** Maps between the domain sale API and core-emv-sale's, nothing more. */
internal class SmartJPaymentSaleEngine : SaleEngine {

    private val coreSaleUseCase by lazy { CoreEmvSaleModule.getUseCase(CoreSaleUseCase::class.java) }

    /** True after a CardForeign/CardNational notification, consumed by the next CurrencyDecision. */
    private var cardIsForeign = false

    override fun sale(request: SaleRequest): Flow<SaleEvent> = flow {
        val decisions = DecisionTracker()
        try {
            coreSaleUseCase(buildEmvSaleParams(request, TerminalSession.params))
                .collect { result -> result.toSaleEvent(decisions)?.let { emit(it) } }
        } finally {
            decisions.cancelAll()
        }
    }

    override suspend fun cancelCardRead() {
        CoreEmvModule.getUseCase(CancelInsertCardUseCase::class.java)()
    }

    private fun SaleTransactionResult.toSaleEvent(decisions: DecisionTracker): SaleEvent? =
        when (this) {
            SaleTransactionResult.Idle -> null
            SaleTransactionResult.InsertCard -> SaleEvent.WaitingForCard
            SaleTransactionResult.Read -> SaleEvent.CardRead
            is SaleTransactionResult.ShowCardLabelDetected -> SaleEvent.CardDetected(cardLabelDescription)
            is SaleTransactionResult.SwipeCard -> SaleEvent.Message(description)
            is SaleTransactionResult.RequestInsertCard -> SaleEvent.Message(description)
            is SaleTransactionResult.SignatureBlocked -> SaleEvent.Message(message)
            is SaleTransactionResult.FollowInstructionTelephone -> SaleEvent.Message(message)
            SaleTransactionResult.RetryWithChip -> {
                TerminalSession.disableContactlessOnNextSale = true
                SaleEvent.RetryWithChip
            }

            is SaleTransactionResult.RequestForMultiApp -> SaleEvent.ApplicationSelectionRequired(
                applications = applications.map { CardApplication(it.id, it.name) },
                decision = asyncDecision(
                    decisionTimeout(TerminalSession.params), decisions,
                    onChoose = { CoreEmvModule.getUseCase(SendApplicationEventUseCase::class.java)(true, it.id) },
                    onCancel = { CoreEmvModule.getUseCase(SendApplicationEventUseCase::class.java)(false, "") },
                ),
            )

            SaleTransactionResult.RequestLastDigits -> cardDigits(CardDigitsType.LAST_FOUR_DIGITS, decisions) {
                CoreEmvModule.getUseCase(SetLastDigitsRequiredUseCase::class.java)(it)
            }

            SaleTransactionResult.RequestCvv -> cardDigits(CardDigitsType.CVV, decisions) {
                CoreEmvModule.getUseCase(SetCvvRequiredUseCase::class.java)(it)
            }

            SaleTransactionResult.NipBegin -> SaleEvent.PinRequested
            is SaleTransactionResult.NipUpdate -> SaleEvent.PinDigitsEntered(number.trim().toIntOrNull() ?: number.length)
            is SaleTransactionResult.NipMessage -> SaleEvent.PinMessage(message)
            is SaleTransactionResult.NipError -> SaleEvent.PinError(description)
            SaleTransactionResult.NipProcess -> SaleEvent.PinProcessing

            SaleTransactionResult.CardForeign -> {
                cardIsForeign = true
                null
            }

            SaleTransactionResult.CardNational -> {
                cardIsForeign = false
                null
            }

            is SaleTransactionResult.CurrencyDecision -> SaleEvent.CurrencyMismatch(
                cardIsForeign = cardIsForeign,
                decision = deferredDecision(desicion) { it },
            )

            is SaleTransactionResult.ShowPromotions -> SaleEvent.PromotionsOffered(
                options = promotionsAvailable.toPublic(),
                decision = deferredDecision(promotionSelectedCompletableDeferred) { it.toInternal() },
            )

            is SaleTransactionResult.ShowDccOffer -> SaleEvent.DccOffered(
                offer = dccOfferAvailable.toPublic(),
                decision = deferredDecision(offerDccSelectedCompletableDeferred) { it.toInternal() },
            )

            is SaleTransactionResult.SaleApproved -> SaleEvent.Approved(resultTransaction.toReceipt())
            is SaleTransactionResult.SaleDenied -> SaleEvent.Declined(
                errorCode = errorCode,
                errorMessage = errorMessage,
                responseCode = responseCode,
                responseMessage = responseMessage,
                suggestQuery = suggestQuery,
                receipt = resultTransaction.toReceipt(),
            )
            is SaleTransactionResult.ShowIntercard ->
                SaleEvent.IntercardRead(intercard.toPublic())
        }

    private fun cardDigits(
        type: CardDigitsType,
        decisions: DecisionTracker,
        send: suspend (String) -> Unit,
    ) = SaleEvent.CardDigitsRequired(
        type = type,
        decision = asyncDecision(
            decisionTimeout(TerminalSession.params), decisions,
            onChoose = send,
            onCancel = { CoreEmvModule.getUseCase(CancelByUserUseCase::class.java)() },
        ),
    )
}

private const val ENABLED_USD = 1

/** core-payments' EMV transaction type for a sale (see e.g. SmartJPayment.java's own "1" checks). */
private const val TRANSACTION_TYPE_SALE = "1"

private fun buildEmvSaleParams(request: SaleRequest, terminalParams: SacNovusParams?): EmvSaleParams {
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
        disableCtls = TerminalSession.disableContactlessOnNextSale
            .also { TerminalSession.disableContactlessOnNextSale = false },
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
        serialNumber = TerminalSession.serialNumber.orEmpty(),
    )
}

private fun PromotionChoice.toInternal(): PromotionSelected = when (this) {
    PromotionChoice.None -> PromotionSelected.WithoutPromotions
    PromotionChoice.LoyaltyPayment -> PromotionSelected.LoyaltyPayment
    is PromotionChoice.MonthsWithoutInterest -> PromotionSelected.MonthsWithoutInterest(months.toString())
    is PromotionChoice.MonthsWithInterest -> PromotionSelected.MonthsWithInterest(months.toString())
}

private fun DccChoice.toInternal(): DccOfferSelected = when (this) {
    DccChoice.FOREIGN_CURRENCY -> DccOfferSelected.DccOfferForeignSelected
    DccChoice.LOCAL_CURRENCY -> DccOfferSelected.DccOfferLocalSelected
}

private fun PromotionsAvailable.toPublic(): PromotionOptions = PromotionOptions(
    monthsWithoutInterest = monthsWithoutInterest.orEmpty().mapNotNull { it.toIntOrNull() },
    monthsWithInterest = monthsWithInterest.orEmpty().mapNotNull { it.toIntOrNull() },
    loyaltyPaymentAvailable = acceptLoyaltyPayment,
)

private fun DccOfferAvailable.toPublic(): DccOffer = DccOffer(
    localAmount = localAmount,
    exchangeRate = exchangeRate,
    mxnVsCurrency = mxnVsCurrency,
    markUp = markUp,
    foreignAmount = foreignAmount,
    foreignCurrency = foreignCurrency,
    reference = reference,
)

private fun IntercardData.toPublic(): Intercard = Intercard(
    uid = uid,
    atqa = atqa,
    sak = sak,
    siteCode = siteCode,
    technology = technology,
    accountNumber = accountNumber,
    encryptedAccountNumber = encryptedAccountNumber
)

/** Adapts a [CompletableDeferred] decision (core-emv-sale) to the domain's [Decision]. */
private fun <Public, Internal> deferredDecision(
    deferred: CompletableDeferred<Internal>,
    map: (Public) -> Internal,
): Decision<Public> = object : Decision<Public> {
    override fun choose(value: Public) {
        deferred.complete(map(value))
    }

    override fun cancel() {
        deferred.cancel()
    }
}
