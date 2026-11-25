package com.smart.smartpay.data.transactions.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    /** Every transaction, newest first. */
    @Query("SELECT * FROM transactions ORDER BY created_at DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    /** The transaction [id] and the operations (cancellation / refunds) made on it. */
    @Query("SELECT * FROM transactions WHERE id = :id OR original_id = :id ORDER BY created_at DESC, id DESC")
    fun observeWithOperations(id: Long): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insert(entity: TransactionEntity): Long
}
