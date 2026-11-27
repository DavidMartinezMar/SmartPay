package com.smart.smartpay.di

import android.content.Context
import androidx.room.Room
import com.smart.smartpay.data.storage.local.TransactionDao
import com.smart.smartpay.data.storage.local.TransactionDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTransactionDatabase(@ApplicationContext context: Context): TransactionDatabase =
        Room.databaseBuilder(context, TransactionDatabase::class.java, TransactionDatabase.NAME).build()

    @Provides
    fun provideTransactionDao(database: TransactionDatabase): TransactionDao =
        database.transactionDao()
}
