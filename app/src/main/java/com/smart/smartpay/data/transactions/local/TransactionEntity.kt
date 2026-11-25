package com.smart.smartpay.data.transactions.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One approved card transaction. Cancellations and refunds point to their sale with [originalId].
 * Only what is needed to list it and to cancel / refund it is stored (never full card data).
 */
@Entity(tableName = "transactions", indices = [Index("original_id")])
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** TransactionType name. */
    val type: String,
    @ColumnInfo(name = "amount_in_cents") val amountInCents: Long,
    @ColumnInfo(name = "tip_in_cents") val tipInCents: Long,
    /** SDK SaleCurrency name. */
    val currency: String,
    @ColumnInfo(name = "authorization_code") val authorizationCode: String,
    val reference: String,
    @ColumnInfo(name = "unique_reference") val uniqueReference: String,
    @ColumnInfo(name = "trans_token") val transToken: String?,
    /** Masked by the host (e.g. "************1234"). */
    @ColumnInfo(name = "masked_pan") val maskedPan: String,
    @ColumnInfo(name = "card_brand") val cardBrand: String,
    /** Epoch millis on the terminal when the transaction was approved. */
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "original_id") val originalId: Long?,
)
