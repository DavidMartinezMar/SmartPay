package com.smart.smartpay.domain.operation

// Commented out temporarily: depended on the common module (device/account persistence).
// TODO: restore once cancellation/refund are migrated off common.
//
// import com.smart.myfinancialapp.data.operation.CardOperation
// import com.smart.myfinancialapp.data.operation.CardOperationRunner
// import com.smart.myfinancialapp.data.operation.EmvCardOperationParams
// import kotlinx.coroutines.flow.Flow
//
// /**
//  * Cancellation (void) of a sale of the same day: the customer presents the card of the sale.
//  *
//  * ```
//  * CancellationUseCase()(CancellationRequest.of(saleReceipt)).collect { event ->
//  *     when (event) {
//  *         CardOperationEvent.WaitingForCard -> showInsertCard()
//  *         is CardOperationEvent.PinRequested -> event.pinPad?.let { showPinPad(it) }
//  *         is CardOperationEvent.Approved -> showReceipt(event.receipt)
//  *         is CardOperationEvent.Declined -> showDeclined(event.errorMessage)
//  *         else -> updateStatus(event)
//  *     }
//  * }
//  * ```
//  *
//  * Same rules as [com.smart.myfinancialapp.domain.payment.SaleUseCase]: the terminal must be
//  * authenticated, one transaction at a time, and keep collecting until the result.
//  */
// class CancellationUseCase internal constructor(
//     private val runner: CardOperationRunner,
//     private val paramsFactory: (CancellationRequest) -> EmvCardOperationParams,
// ) {
//
//     constructor() : this(CardOperationRunner.default(), ::defaultParams)
//
//     operator fun invoke(request: CancellationRequest): Flow<CardOperationEvent> =
//         runner.run { paramsFactory(request) }
//
//     /** Cancels while waiting for the card. The flow then ends with [CardOperationEvent.Declined]. */
//     suspend fun cancel() {
//         runner.cancel()
//     }
//
//     private companion object {
//         fun defaultParams(request: CancellationRequest): EmvCardOperationParams =
//             CardOperationRunner.defaultParams(
//                 operation = request.toOperation(),
//                 uniqueReference = request.uniqueReference,
//                 currency = request.currency,
//             )
//     }
// }
//
// internal fun CancellationRequest.toOperation(): CardOperation.Cancellation =
//     CardOperation.Cancellation(reference = reference, transToken = transToken.orEmpty())
