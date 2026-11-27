package com.smart.smartpay.data.storage.local

import androidx.room.Database
import androidx.room.RoomDatabase

/** Transaction history. Separate from the configuration database so each can evolve on its own. */
@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "transactions.db"
    }
}
