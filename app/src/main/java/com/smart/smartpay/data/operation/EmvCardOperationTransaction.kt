package com.smart.smartpay.data.operation

// Commented out temporarily: depended on the common module (device/account persistence).
// TODO: restore once cancellation/refund are migrated off common.
//
// import com.smart.common.data.utils.TRANSACTION_CANCELLATION
// import com.smart.common.data.utils.TRANSACTION_REFUND
// import com.smart.core_emv.data.mappers.toOperationParams
// import com.smart.core_emv.data.models.AMOUNT_KEY
// import com.smart.core_emv.data.models.APPROVAL_CODE_KEY
// import com.smart.core_emv.data.models.CASHIER_NAME_KEY
// import com.smart.core_emv.data.models.CURRENCY_CODE_KEY
// import com.smart.core_emv.data.models.ENTRY_MODE_FLAG_KEY
// import com.smart.core_emv.data.models.MODULE_TRANSACTION_NAME
// import com.smart.core_emv.data.models.PIN_TAP_MAP_KEY
// import com.smart.core_emv.data.models.REFERENCE_NUMBER_KEY
// import com.smart.core_emv.data.models.TRANSACTION_DETAIL_KEY
// import com.smart.core_emv.data.models.TRANSACTION_TIME_OUT_KEY
// import com.smart.core_emv.data.models.TRANSACTION_TYPE_KEY
// import com.smart.core_emv.data.models.TRANS_TOKEN_KEY
// import com.smart.core_emv.data.models.TXN_RESTRICTIONS_KEY
// import com.smart.core_emv.data.models.TXN_UNIQUE_REF_KEY
// import com.smart.core_emv.data.models.collectionEmvParameters
// import com.smart.core_emv.data.utils.getErrorCode
// import com.smart.core_emv.data.utils.getErrorMessage
// import com.smart.core_emv.data.utils.getParametersTransaction
// import com.smart.core_emv.data.utils.getResponseCode
// import com.smart.core_emv.data.utils.getResponseMessage
// import com.smart.core_emv.data.utils.isSuggest
// import com.smart.core_emv.domain.models.FlapDynamicFields
// import com.smart.core_emv.utils.getStringEventParamAsBoolean
// import com.smart.core_payments.smartJPayment.Parameters
// import com.smart.core_payments.smartJPayment.SmartJPayment
// import com.smart.core_payments.smartJPayment.SmartJPaymentEvent
// import com.smart.core_payments.smartJPayment.TransactionImpl
// import com.smart.myfinancialapp.data.payment.toReceipt
// import com.smart.myfinancialapp.domain.operation.CardOperationEvent
// import kotlinx.coroutines.channels.ProducerScope
// import kotlinx.coroutines.channels.awaitClose
// import kotlinx.coroutines.flow.Flow
// import kotlinx.coroutines.flow.callbackFlow
//
// /**
//  * Cancellation / refund transaction on core-payments: port of SmartWrapper's
//  * cancellation/EmvCancellation and refund/EmvRefund without those modules.
//  *
//  * SmartJPayment runs the same INITIALIZE → BEGIN_TRANSACTION → GET_BANK_AUTHORIZATION →
//  * END_TRANSACTION flow as a sale; core-payments recognizes the operation by TRANSACTION_TYPE
//  * (no preventive reversal, no promotions / DCC). No callback here blocks the SDK thread.
//  */
// internal class EmvCardOperationTransaction(private val smartJPayment: SmartJPayment) {
//
//     fun run(params: EmvCardOperationParams): Flow<CardOperationEvent> = callbackFlow {
//         smartJPayment.submit(MODULE_TRANSACTION_NAME, OperationTransaction(params, this))
//         awaitClose {}
//     }
//
//     private inner class OperationTransaction(
//         private val operation: EmvCardOperationParams,
//         private val events: ProducerScope<CardOperationEvent>,
//     ) : TransactionImpl() {
//
//         override fun onBeforeBeginTransactionEvent(event: SmartJPaymentEvent?, params: Parameters) {
//             super.onBeforeBeginTransactionEvent(event, params)
//             beginTransactionValues(operation).forEach { (key, value) -> params.setOperationParam(key, value) }
//             if (operation.disableContactless) {
//                 // After a contactless failure (RC=65): disable RF on the next card wait
//                 params.setOperationParam(CTLS_ERR_RETRY_KEY, "1")
//             }
//         }
//
//         override fun onBeforeGetBankAuthorizationEvent(event: SmartJPaymentEvent?, params: Parameters?) {
//             super.onBeforeGetBankAuthorizationEvent(event, params)
//             params?.setOperationParam(TXN_UNIQUE_REF_KEY, operation.uniqueReference)
//         }
//
//         override fun onTransactionCompleteEvent(event: SmartJPaymentEvent?, params: Parameters?) {
//             super.onTransactionCompleteEvent(event, params)
//             val receipt = getParametersTransaction(smartJPayment, collectionEmvParameters).toReceipt()
//             val result = if (getStringEventParamAsBoolean(event, SUCCESS_KEY, false)) {
//                 CardOperationEvent.Approved(receipt)
//             } else {
//                 CardOperationEvent.Declined(
//                     errorCode = getErrorCode(params),
//                     errorMessage = getErrorMessage(params),
//                     responseCode = getResponseCode(params),
//                     responseMessage = getResponseMessage(params),
//                     suggestQuery = isSuggest(params),
//                     receipt = receipt,
//                 )
//             }
//             events.trySend(result)
//             events.close()
//         }
//     }
//
//     internal companion object {
//         const val SUCCESS_KEY = "SUCCESS"
//         const val CTLS_ERR_RETRY_KEY = "CTLS_ERR_RETRY"
//
//         /** Operation params set before reading the card (same keys and values as SmartWrapper). */
//         fun beginTransactionValues(params: EmvCardOperationParams): Map<String, String> =
//             when (val operation = params.operation) {
//                 is CardOperation.Cancellation -> mapOf(
//                     TRANSACTION_TYPE_KEY to TRANSACTION_CANCELLATION,
//                     REFERENCE_NUMBER_KEY to operation.reference,
//                     CURRENCY_CODE_KEY to params.currencyCode,
//                     PIN_TAP_MAP_KEY to params.pinTapMap,
//                     TRANS_TOKEN_KEY to operation.transToken,
//                     ENTRY_MODE_FLAG_KEY to params.entryModeFlag.toString(),
//                 ) + FlapDynamicFields().toOperationParams()
//
//                 is CardOperation.Refund -> mapOf(
//                     TRANSACTION_TYPE_KEY to TRANSACTION_REFUND,
//                     AMOUNT_KEY to operation.amountInCents.toString(),
//                     CURRENCY_CODE_KEY to params.currencyCode,
//                     APPROVAL_CODE_KEY to operation.approvalCode,
//                     TRANSACTION_DETAIL_KEY to "",
//                     CASHIER_NAME_KEY to operation.cashierName,
//                     PIN_TAP_MAP_KEY to params.pinTapMap,
//                     TXN_RESTRICTIONS_KEY to "",
//                     TRANSACTION_TIME_OUT_KEY to "",
//                     TRANS_TOKEN_KEY to operation.transToken,
//                     ENTRY_MODE_FLAG_KEY to params.entryModeFlag.toString(),
//                 )
//             }
//     }
// }
