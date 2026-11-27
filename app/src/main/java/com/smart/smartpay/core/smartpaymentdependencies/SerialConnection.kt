package com.smart.smartpay.core.smartpaymentdependencies

import com.smart.core_payments.domain.repository.SerialConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.time.Duration

/**
 * No-op Serial is only used by core-payments for external pinpads (DLLTerminalClient, off-line authentication flavor).
 */
internal object SerialConnection : SerialConnection {
    override suspend fun write(data: ByteArray): Result<Boolean> = Result.success(false)
    override suspend fun writeFlat(data: ByteArray): Result<Boolean> = Result.success(false)
    override suspend fun read(timeoutMs: Duration): Flow<ByteArray> = emptyFlow()
    override fun abortRead() = Unit
}