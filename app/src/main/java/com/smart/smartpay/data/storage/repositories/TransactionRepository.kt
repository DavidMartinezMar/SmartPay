package com.smart.smartpay.data.storage.repositories

import com.smart.smartpay.data.storage.model.TransactionRecord
import com.smart.smartpay.data.storage.model.TransactionType
import com.smart.smartpay.domain.models.SaleReceipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** History of approved transactions (sales and the cancellations / refunds made on them). */
interface TransactionRepository {

    /** Every transaction, newest first; sales carry their operations. */
    fun observeHistory(): Flow<List<TransactionRecord>>

    /** Transaction [id] with its operations; null if it doesn't exist. */
    fun observeTransaction(id: Long): Flow<TransactionRecord?>

    suspend fun getTransaction(id: Long): TransactionRecord? = observeTransaction(id).first()

    /** Saves an approved sale and returns its id. */
    suspend fun saveSale(receipt: SaleReceipt, amountInCents: Long, tipInCents: Long): Long

    /** Saves an approved cancellation or refund of the sale [originalId] and returns its id. */
    suspend fun saveOperation(originalId: Long, type: TransactionType, receipt: SaleReceipt, amountInCents: Long): Long
}
