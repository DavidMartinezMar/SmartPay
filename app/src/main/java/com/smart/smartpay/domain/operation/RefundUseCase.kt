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
//  * Refund (devolución) of a sale, total or partial: the customer presents the card of the sale.
//  *
//  * ```
//  * RefundUseCase()(RefundRequest.of(saleReceipt, amountInCents = 5000)).collect { event ->
//  *     when (event) {
//  *         CardOperationEvent.WaitingForCard -> showInsertCard()
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
// class RefundUseCase internal constructor(
//     private val runner: CardOperationRunner,
//     private val paramsFactory: (RefundRequest) -> EmvCardOperationParams,
// ) {
//
//     constructor() : this(CardOperationRunner.default(), ::defaultParams)
//
//     operator fun invoke(request: RefundRequest): Flow<CardOperationEvent> =
//         runner.run { paramsFactory(request) }
//
//     /** Cancels while waiting for the card. The flow then ends with [CardOperationEvent.Declined]. */
//     suspend fun cancel() {
//         runner.cancel()
//     }
//
//     private companion object {
//         fun defaultParams(request: RefundRequest): EmvCardOperationParams =
//             CardOperationRunner.defaultParams(
//                 operation = request.toOperation(),
//                 uniqueReference = request.uniqueReference,
//                 currency = request.currency,
//             )
//     }
// }
//
// internal fun RefundRequest.toOperation(): CardOperation.Refund =
//     CardOperation.Refund(
//         amountInCents = amountInCents,
//         approvalCode = approvalCode,
//         cashierName = cashierName.orEmpty(),
//         transToken = transToken.orEmpty(),
//     )
