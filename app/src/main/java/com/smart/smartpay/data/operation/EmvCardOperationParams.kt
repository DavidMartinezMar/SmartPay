package com.smart.smartpay.data.operation

// Commented out temporarily: depended on the common module (device/account persistence).
// TODO: restore once cancellation/refund are migrated off common.
//
// import com.smart.core.business.model.profiler.SacNovusParams
// import com.smart.myfinancialapp.data.payment.decisionTimeout
// import com.smart.myfinancialapp.data.payment.entryModeFlag
// import com.smart.myfinancialapp.domain.payment.SaleCurrency
// import com.smart.myfinancialapp.ui.pinpad.PinPadGeometry
// import com.smart.myfinancialapp.ui.pinpad.PinPadLayout
// import kotlin.time.Duration
//
// /** What changes between a cancellation and a refund (SmartWrapper's EmvCancellation / EmvRefund). */
// internal sealed interface CardOperation {
//
//     /** [reference]: host reference of the sale (SG_REFERENCE). */
//     data class Cancellation(val reference: String, val transToken: String) : CardOperation
//
//     data class Refund(
//         val amountInCents: Long,
//         val approvalCode: String,
//         val cashierName: String,
//         val transToken: String,
//     ) : CardOperation
// }
//
// /** Everything a cancellation or refund sends to core-payments. */
// internal data class EmvCardOperationParams(
//     val operation: CardOperation,
//     /** TXN_UNIQUE_REF of the original sale. */
//     val uniqueReference: String,
//     val currencyCode: String,
//     val pinTapMap: String,
//     /** Null when the terminal shows its own secure PIN screen. */
//     val pinPad: PinPadLayout?,
//     val entryModeFlag: Int,
//     val disableContactless: Boolean,
//     val decisionTimeout: Duration,
// )
//
// internal fun buildEmvCardOperationParams(
//     operation: CardOperation,
//     uniqueReference: String,
//     currency: SaleCurrency,
//     terminalParams: SacNovusParams?,
//     digits: List<Int>,
//     geometry: PinPadGeometry,
//     pinOnAppScreen: Boolean,
//     disableContactless: Boolean,
// ): EmvCardOperationParams = EmvCardOperationParams(
//     operation = operation,
//     uniqueReference = uniqueReference,
//     currencyCode = currency.code,
//     pinTapMap = geometry.toTapMap(digits),
//     pinPad = if (pinOnAppScreen) {
//         // A cancellation doesn't carry the sale amount: the pad shows no amount
//         PinPadLayout(digits, geometry, (operation as? CardOperation.Refund)?.amountInCents, currency)
//     } else {
//         null
//     },
//     entryModeFlag = entryModeFlag(terminalParams),
//     disableContactless = disableContactless,
//     decisionTimeout = decisionTimeout(terminalParams),
// )
