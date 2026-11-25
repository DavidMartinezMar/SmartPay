package com.smart.smartpay.domain.operation

// Commented out temporarily: depended on the common module (device/account persistence).
// TODO: restore once cancellation/refund are migrated off common.
//
// import com.smart.myfinancialapp.domain.payment.CardApplication
// import com.smart.myfinancialapp.domain.payment.CardDigitsType
// import com.smart.myfinancialapp.domain.payment.Decision
// import com.smart.myfinancialapp.domain.payment.SaleReceipt
// import com.smart.myfinancialapp.ui.pinpad.PinPadLayout
//
// /**
//  * Progress of a card operation on an existing sale: cancellation
//  * ([com.smart.myfinancialapp.domain.operation.CancellationUseCase]) or refund
//  * ([com.smart.myfinancialapp.domain.operation.RefundUseCase]).
//  *
//  * Same card reading and PIN events as a sale, without its customer questions (promotions, DCC,
//  * currency). It ends with [Approved], [Declined] or [Failed].
//  */
// sealed interface CardOperationEvent {
//
//     // --- Card reading ---
//
//     /** Waiting for the customer to insert, swipe or tap the card of the original sale. */
//     data object WaitingForCard : CardOperationEvent
//
//     data object CardRead : CardOperationEvent
//
//     /** Card brand/label detected (e.g. "VISA DEBITO"). */
//     data class CardDetected(val label: String) : CardOperationEvent
//
//     /** Instruction or information to show. */
//     data class Message(val text: String) : CardOperationEvent
//
//     /**
//      * Contactless failed: the operation will end declined; start it again and ask for chip.
//      * Contactless is disabled for that next transaction.
//      */
//     data object RetryWithChip : CardOperationEvent
//
//     data class ApplicationSelectionRequired(
//         val applications: List<CardApplication>,
//         val decision: Decision<CardApplication>,
//     ) : CardOperationEvent
//
//     data class CardDigitsRequired(
//         val type: CardDigitsType,
//         val decision: Decision<String>,
//     ) : CardOperationEvent
//
//     // --- PIN ---
//
//     /**
//      * The customer must enter the PIN. [pinPad] is not null on terminals where the PIN is typed
//      * on the app screen: show it right away.
//      */
//     data class PinRequested(val pinPad: PinPadLayout?) : CardOperationEvent
//
//     data class PinDigitsEntered(val count: Int) : CardOperationEvent
//
//     data class PinMessage(val text: String) : CardOperationEvent
//
//     data class PinError(val message: String) : CardOperationEvent
//
//     /** PIN captured; dismiss the PIN pad. */
//     data object PinProcessing : CardOperationEvent
//
//     // --- Result ---
//
//     data class Approved(val receipt: SaleReceipt) : CardOperationEvent
//
//     /**
//      * Declined by the host or the card, or cancelled.
//      *
//      * For a cancellation, [responseCode] [ALREADY_CANCELLED_RESPONSE_CODE] means the sale was
//      * already cancelled.
//      */
//     data class Declined(
//         val errorCode: String,
//         val errorMessage: String,
//         val responseCode: String,
//         val responseMessage: String,
//         /** The host suggests querying the transaction status before retrying. */
//         val suggestQuery: Boolean,
//         val receipt: SaleReceipt,
//     ) : CardOperationEvent
//
//     /** The operation could not start (see [com.smart.myfinancialapp.domain.payment.SaleErrorCodes]). */
//     data class Failed(val code: String, val message: String) : CardOperationEvent
//
//     companion object {
//         /** Host response code of a cancellation whose sale was already cancelled. */
//         const val ALREADY_CANCELLED_RESPONSE_CODE: String = "67"
//     }
// }
