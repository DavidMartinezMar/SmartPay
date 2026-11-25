package com.smart.smartpay.data.operation

// Commented out temporarily: depended on the common module (device/account persistence).
// TODO: restore once cancellation/refund are migrated off common.
//
// import com.smart.core_emv.data.models.EmvNotification
// import com.smart.core_emv.di.CoreEmvModule
// import com.smart.core_emv.domain.usecases.CancelByUserUseCase
// import com.smart.core_emv.domain.usecases.CancelInsertCardUseCase
// import com.smart.core_emv.domain.usecases.SendApplicationEventUseCase
// import com.smart.core_emv.domain.usecases.SetCvvRequiredUseCase
// import com.smart.core_emv.domain.usecases.SetLastDigitsRequiredUseCase
// import com.smart.myfinancialapp.core.PaymentSdk
// import com.smart.myfinancialapp.core.TerminalSession
// import com.smart.myfinancialapp.core.TerminalUi
// import com.smart.myfinancialapp.core.TransactionLock
// import com.smart.myfinancialapp.data.payment.DecisionTracker
// import com.smart.myfinancialapp.data.payment.asyncDecision
// import com.smart.myfinancialapp.domain.operation.CardOperationEvent
// import com.smart.myfinancialapp.domain.payment.CardApplication
// import com.smart.myfinancialapp.domain.payment.CardDigitsType
// import com.smart.myfinancialapp.domain.payment.SaleCurrency
// import com.smart.myfinancialapp.domain.payment.SaleErrorCodes
// import com.smart.myfinancialapp.ui.pinpad.PinPadGeometry
// import kotlinx.coroutines.CompletableDeferred
// import kotlinx.coroutines.flow.Flow
// import kotlinx.coroutines.flow.channelFlow
// import kotlinx.coroutines.flow.emitAll
// import kotlinx.coroutines.flow.flow
// import kotlinx.coroutines.flow.onSubscription
// import kotlinx.coroutines.launch
//
// /** Runs a cancellation or refund on the terminal. */
// internal interface CardOperationEngine {
//     fun run(params: EmvCardOperationParams): Flow<CardOperationEvent>
//
//     /** Cancels while waiting for the card. */
//     suspend fun cancelCardRead()
// }
//
// /**
//  * Real engine: the operation transaction plus the card reading / PIN events that common's Emv
//  * publishes (as SmartWrapper's EmvCancellationRepositoryImpl / EmvRefundRepositoryImpl merge them).
//  */
// internal class SmartJPaymentCardOperationEngine : CardOperationEngine {
//
//     override fun run(params: EmvCardOperationParams): Flow<CardOperationEvent> = channelFlow {
//         val decisions = DecisionTracker()
//         try {
//             // Subscribe to Emv events before starting: they are not replayed
//             val subscribed = CompletableDeferred<Unit>()
//             val emvEvents = launch {
//                 CoreEmvModule.emv.emvNotificationEvent
//                     .onSubscription { subscribed.complete(Unit) }
//                     .collect { notification -> notification.toOperationEvent(params, decisions)?.let { send(it) } }
//             }
//             subscribed.await()
//
//             EmvCardOperationTransaction(CoreEmvModule.smartJPayment).run(params).collect { send(it) }
//             emvEvents.cancel()
//         } finally {
//             decisions.cancelAll()
//         }
//     }
//
//     override suspend fun cancelCardRead() {
//         CoreEmvModule.getUseCase(CancelInsertCardUseCase::class.java)()
//     }
//
//     private fun EmvNotification.toOperationEvent(
//         params: EmvCardOperationParams,
//         decisions: DecisionTracker,
//     ): CardOperationEvent? = when (this) {
//         EmvNotification.InsertCard -> CardOperationEvent.WaitingForCard
//         EmvNotification.CardRead -> CardOperationEvent.CardRead
//         is EmvNotification.ShowLabelCardDetected -> CardOperationEvent.CardDetected(labelCardDescription)
//         is EmvNotification.SwipeCard -> CardOperationEvent.Message(description)
//         is EmvNotification.RequestInsertCard -> CardOperationEvent.Message(description)
//         is EmvNotification.SignatureBlocked -> CardOperationEvent.Message(message)
//         is EmvNotification.FollowInstructionTelephone -> CardOperationEvent.Message(message)
//         EmvNotification.RetryWithChip -> {
//             TerminalSession.disableContactlessOnNextSale = true
//             CardOperationEvent.RetryWithChip
//         }
//         is EmvNotification.RequestForMultiApp -> CardOperationEvent.ApplicationSelectionRequired(
//             applications = applications.map { CardApplication(it.id, it.name) },
//             decision = asyncDecision<CardApplication>(
//                 params.decisionTimeout, decisions,
//                 onChoose = { CoreEmvModule.getUseCase(SendApplicationEventUseCase::class.java)(true, it.id) },
//                 onCancel = { CoreEmvModule.getUseCase(SendApplicationEventUseCase::class.java)(false, "") },
//             ),
//         )
//         EmvNotification.RequestLastDigits -> cardDigits(CardDigitsType.LAST_FOUR_DIGITS, params, decisions) {
//             CoreEmvModule.getUseCase(SetLastDigitsRequiredUseCase::class.java)(it)
//         }
//         EmvNotification.RequestCvv -> cardDigits(CardDigitsType.CVV, params, decisions) {
//             CoreEmvModule.getUseCase(SetCvvRequiredUseCase::class.java)(it)
//         }
//         EmvNotification.NipBegin -> CardOperationEvent.PinRequested(params.pinPad)
//         is EmvNotification.NipUpdate -> CardOperationEvent.PinDigitsEntered(number.trim().toIntOrNull() ?: number.length)
//         is EmvNotification.NipMessage -> CardOperationEvent.PinMessage(message)
//         is EmvNotification.NipError -> CardOperationEvent.PinError(description)
//         EmvNotification.NipProcess -> CardOperationEvent.PinProcessing
//         EmvNotification.Idle,
//         is EmvNotification.ShowVoucherHeadersFooters,
//         is EmvNotification.HostTimeUpdate,
//         -> null
//     }
//
//     private fun cardDigits(
//         type: CardDigitsType,
//         params: EmvCardOperationParams,
//         decisions: DecisionTracker,
//         send: suspend (String) -> Unit,
//     ) = CardOperationEvent.CardDigitsRequired(
//         type = type,
//         decision = asyncDecision(
//             params.decisionTimeout, decisions,
//             onChoose = send,
//             onCancel = { CoreEmvModule.getUseCase(CancelByUserUseCase::class.java)() },
//         ),
//     )
// }
//
// /**
//  * Checks shared by [com.smart.myfinancialapp.domain.operation.CancellationUseCase] and
//  * [com.smart.myfinancialapp.domain.operation.RefundUseCase]: SDK initialized, terminal
//  * authenticated and no other transaction (sale included) running.
//  */
// internal class CardOperationRunner(
//     private val engine: () -> CardOperationEngine,
//     private val isSdkInitialized: () -> Boolean,
//     private val isTerminalReady: () -> Boolean,
// ) {
//
//     fun run(params: () -> EmvCardOperationParams): Flow<CardOperationEvent> = flow {
//         when {
//             !isSdkInitialized() -> emit(failed(SaleErrorCodes.NOT_INITIALIZED, "Call PaymentSdk.initialize(application) first"))
//             !isTerminalReady() -> emit(failed(SaleErrorCodes.TERMINAL_NOT_READY, "Authenticate the terminal first"))
//             !TransactionLock.tryAcquire() -> emit(failed(SaleErrorCodes.SALE_IN_PROGRESS, "Another transaction is in progress"))
//             else -> try {
//                 emitAll(engine().run(params()))
//             } finally {
//                 TransactionLock.release()
//             }
//         }
//     }
//
//     suspend fun cancel() {
//         if (isSdkInitialized()) engine().cancelCardRead()
//     }
//
//     private fun failed(code: String, message: String) = CardOperationEvent.Failed(code, message)
//
//     companion object {
//         fun default(): CardOperationRunner = CardOperationRunner(
//             engine = { SmartJPaymentCardOperationEngine() },
//             isSdkInitialized = { PaymentSdk.isInitialized },
//             isTerminalReady = { TerminalSession.isReadyForSale },
//         )
//
//         /** Params of the terminal in use (random PIN pad, entry modes, timeouts). */
//         fun defaultParams(
//             operation: CardOperation,
//             uniqueReference: String,
//             currency: SaleCurrency,
//         ): EmvCardOperationParams = buildEmvCardOperationParams(
//             operation = operation,
//             uniqueReference = uniqueReference,
//             currency = currency,
//             terminalParams = TerminalSession.params,
//             digits = (0..9).shuffled(),
//             geometry = PinPadGeometry.fromDisplay(PaymentSdk.applicationContext),
//             pinOnAppScreen = TerminalUi.pinOnAppScreen,
//             disableContactless = TerminalSession.disableContactlessOnNextSale
//                 .also { TerminalSession.disableContactlessOnNextSale = false },
//         )
//     }
// }
