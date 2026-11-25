package com.smart.smartpay.domain.payment

/** Progress of a sale. A sale ends with [Approved], [Declined] or [Failed]. */
sealed interface SaleEvent {

    // --- Card reading ---

    /** Waiting for the customer to insert, swipe or tap the card. */
    data object WaitingForCard : SaleEvent

    data object CardRead : SaleEvent

    /** Card brand/label detected (e.g. "VISA DEBITO"). */
    data class CardDetected(val label: String) : SaleEvent

    /** Instruction or information to show (e.g. "Inserte la tarjeta", "Siga las instrucciones en su teléfono"). */
    data class Message(val text: String) : SaleEvent

    /**
     * Contactless failed: the sale will end declined; start it again and ask for chip.
     * Contactless is disabled for that next sale.
     */
    data object RetryWithChip : SaleEvent

    data class ApplicationSelectionRequired(
        val applications: List<CardApplication>,
        val decision: Decision<CardApplication>,
    ) : SaleEvent

    data class CardDigitsRequired(
        val type: CardDigitsType,
        val decision: Decision<String>,
    ) : SaleEvent

    // --- Customer decisions ---

    /**
     * The card currency doesn't match the sale currency ([cardIsForeign]: foreign card on a peso
     * sale, otherwise national card on a dollar sale). Choose true to continue, false to abort.
     */
    data class CurrencyMismatch(
        val cardIsForeign: Boolean,
        val decision: Decision<Boolean>,
    ) : SaleEvent

    data class PromotionsOffered(
        val options: PromotionOptions,
        val decision: Decision<PromotionChoice>,
    ) : SaleEvent

    data class DccOffered(
        val offer: DccOffer,
        val decision: Decision<DccChoice>,
    ) : SaleEvent

    // --- PIN ---

    /** The customer must enter the PIN on the terminal's own secure PIN screen. */
    data object PinRequested : SaleEvent

    data class PinDigitsEntered(val count: Int) : SaleEvent

    data class PinMessage(val text: String) : SaleEvent

    data class PinError(val message: String) : SaleEvent

    /** PIN captured; dismiss the PIN pad. */
    data object PinProcessing : SaleEvent

    // --- Result ---

    data class Approved(val receipt: SaleReceipt) : SaleEvent

    /** Declined by the host or the card, or cancelled. */
    data class Declined(
        val errorCode: String,
        val errorMessage: String,
        val responseCode: String,
        val responseMessage: String,
        /** The host suggests querying the transaction status before retrying. */
        val suggestQuery: Boolean,
        val receipt: SaleReceipt,
    ) : SaleEvent

    /** The sale could not start (see [SaleErrorCodes]). */
    data class Failed(val code: String, val message: String) : SaleEvent

    data class IntercardRead(
        val intercard: Intercard
    ) : SaleEvent
}

object SaleErrorCodes {
    const val NOT_INITIALIZED: String = "SDK_NOT_INITIALIZED"

    /** The terminal was not authenticated in this process (AuthenticateProfilerUseCase). */
    const val TERMINAL_NOT_READY: String = "SDK_TERMINAL_NOT_READY"

    /** Another sale is in progress. */
    const val SALE_IN_PROGRESS: String = "SDK_SALE_IN_PROGRESS"
}
