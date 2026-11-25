package com.smart.smartpay.domain.operation

// Commented out temporarily: depended on the common module (device/account persistence).
// TODO: restore once cancellation/refund are migrated off common.
//
// import com.smart.myfinancialapp.domain.payment.SaleCurrency
// import com.smart.myfinancialapp.domain.payment.SaleReceipt
// import com.smart.myfinancialapp.data.payment.currencyOf
//
// /**
//  * Sale to cancel (void). The host only accepts it before the batch is closed (same day).
//  * Build it from the sale receipt with [of].
//  *
//  * @property reference host reference of the sale ([SaleReceipt.reference]).
//  * @property uniqueReference unique reference of the sale ([SaleReceipt.uniqueReference]).
//  * @property transToken token the host returned with the sale, if any.
//  */
// data class CancellationRequest(
//     val reference: String,
//     val uniqueReference: String,
//     val currency: SaleCurrency = SaleCurrency.MXN,
//     val transToken: String? = null,
// ) {
//     init {
//         require(reference.isNotBlank()) { "reference can't be blank" }
//         require(uniqueReference.isNotBlank()) { "uniqueReference can't be blank" }
//     }
//
//     companion object {
//         /** Cancellation of the sale of [receipt]. */
//         fun of(receipt: SaleReceipt): CancellationRequest = CancellationRequest(
//             reference = receipt.reference,
//             uniqueReference = receipt.uniqueReference,
//             currency = currencyOf(receipt),
//             transToken = receipt.raw[TRANS_TOKEN]?.takeIf { it.isNotBlank() },
//         )
//
//         private const val TRANS_TOKEN = "TRANS_TOKEN"
//     }
// }
