package com.smart.smartpay.domain.operation

// Commented out temporarily: depended on the common module (device/account persistence).
// TODO: restore once cancellation/refund are migrated off common.
//
// import com.smart.myfinancialapp.domain.payment.SaleCurrency
// import com.smart.myfinancialapp.domain.payment.SaleReceipt
// import com.smart.myfinancialapp.data.payment.currencyOf
//
// /**
//  * Refund (devolución) of a sale, total or partial. Build it from the sale receipt with [of].
//  *
//  * @property amountInCents amount to give back, in cents (e.g. 12550 = $125.50).
//  * @property approvalCode authorization code of the sale ([SaleReceipt.authorizationCode]).
//  * @property uniqueReference unique reference of the sale ([SaleReceipt.uniqueReference]).
//  * @property transToken token the host returned with the sale, if any.
//  */
// data class RefundRequest(
//     val amountInCents: Long,
//     val approvalCode: String,
//     val uniqueReference: String,
//     val currency: SaleCurrency = SaleCurrency.MXN,
//     val cashierName: String? = null,
//     val transToken: String? = null,
// ) {
//     init {
//         require(amountInCents > 0) { "amountInCents must be greater than 0" }
//         require(approvalCode.isNotBlank()) { "approvalCode can't be blank" }
//         require(uniqueReference.isNotBlank()) { "uniqueReference can't be blank" }
//     }
//
//     companion object {
//         /** Refund of [amountInCents] of the sale of [receipt]. */
//         fun of(receipt: SaleReceipt, amountInCents: Long, cashierName: String? = null): RefundRequest =
//             RefundRequest(
//                 amountInCents = amountInCents,
//                 approvalCode = receipt.authorizationCode,
//                 uniqueReference = receipt.uniqueReference,
//                 currency = currencyOf(receipt),
//                 cashierName = cashierName,
//                 transToken = receipt.raw[TRANS_TOKEN]?.takeIf { it.isNotBlank() },
//             )
//
//         private const val TRANS_TOKEN = "TRANS_TOKEN"
//     }
// }
