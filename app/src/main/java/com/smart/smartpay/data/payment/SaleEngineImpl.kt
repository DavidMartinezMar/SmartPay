package com.smart.smartpay.data.payment

import com.smart.core_emv.di.CoreEmvModule
import com.smart.core_emv.domain.usecases.CancelByUserUseCase
import com.smart.core_emv.domain.usecases.CancelInsertCardUseCase
import com.smart.core_emv.domain.usecases.SendApplicationEventUseCase
import com.smart.core_emv.domain.usecases.SetCvvRequiredUseCase
import com.smart.core_emv.domain.usecases.SetLastDigitsRequiredUseCase
import com.smart.core_emv_sale.di.CoreEmvSaleModule
import com.smart.core_emv_sale.domain.models.SaleTransactionResult
import com.smart.smartpay.core.TerminalSessionManager
import com.smart.smartpay.data.models.DecisionTracker
import com.smart.smartpay.data.models.deferredDecision
import com.smart.smartpay.data.utils.asyncDecision
import com.smart.smartpay.data.utils.buildEmvSaleParams
import com.smart.smartpay.data.utils.decisionTimeout
import com.smart.smartpay.data.utils.extensionfunctions.toPublic
import com.smart.smartpay.domain.models.CardApplication
import com.smart.smartpay.domain.models.CardDigitsType
import com.smart.smartpay.domain.payment.SaleEvent
import com.smart.smartpay.domain.models.SaleRequest
import com.smart.smartpay.domain.utils.extensionfunctions.toInternal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.smart.core_emv_sale.domain.usecases.SaleUseCase as CoreSaleUseCase

/** Maps between the domain sale API and core-emv-sale's, nothing more. */
internal class SmartJPaymentSaleEngine : SaleEngine {
    private val coreSaleUseCase by lazy { CoreEmvSaleModule.getUseCase(CoreSaleUseCase::class.java) }
    /** True after a CardForeign/CardNational notification, consumed by the next CurrencyDecision. */
    private var cardIsForeign = false

    override fun sale(request: SaleRequest): Flow<SaleEvent> = flow {
        val decisions = DecisionTracker()
        try {
            coreSaleUseCase(buildEmvSaleParams(request, TerminalSessionManager.params))
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
                TerminalSessionManager.disableContactlessOnNextSale = true
                SaleEvent.RetryWithChip
            }

            is SaleTransactionResult.RequestForMultiApp -> SaleEvent.ApplicationSelectionRequired(
                applications = applications.map { CardApplication(it.id, it.name) },
                decision = asyncDecision(
                    decisionTimeout(TerminalSessionManager.params), decisions,
                    onChoose = {
                        CoreEmvModule.getUseCase(SendApplicationEventUseCase::class.java)(
                            true,
                            it.id
                        )
                    },
                    onCancel = {
                        CoreEmvModule.getUseCase(SendApplicationEventUseCase::class.java)(
                            false,
                            ""
                        )
                    },
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
            decisionTimeout(TerminalSessionManager.params), decisions,
            onChoose = send,
            onCancel = { CoreEmvModule.getUseCase(CancelByUserUseCase::class.java)() },
        ),
    )
}
