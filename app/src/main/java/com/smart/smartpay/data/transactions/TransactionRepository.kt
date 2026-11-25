package com.smart.smartpay.data.transactions

import com.smart.smartpay.data.transactions.local.TransactionDao
import com.smart.smartpay.data.transactions.local.TransactionEntity
import com.smart.smartpay.data.transactions.model.TransactionRecord
import com.smart.smartpay.data.transactions.model.TransactionType
import com.smart.smartpay.domain.payment.SaleCurrency
import com.smart.smartpay.domain.payment.SaleReceipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

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

class TransactionRepositoryImpl internal constructor(
    private val dao: TransactionDao,
    private val now: () -> Long,
) : TransactionRepository {

    @Inject
    constructor(dao: TransactionDao) : this(dao, System::currentTimeMillis)

    override fun observeHistory(): Flow<List<TransactionRecord>> =
        dao.observeAll().map { it.toRecords() }

    override fun observeTransaction(id: Long): Flow<TransactionRecord?> =
        dao.observeWithOperations(id).map { rows -> rows.toRecords().firstOrNull { it.id == id } }

    override suspend fun saveSale(receipt: SaleReceipt, amountInCents: Long, tipInCents: Long): Long =
        dao.insert(receipt.toEntity(TransactionType.SALE, amountInCents, tipInCents, originalId = null))

    override suspend fun saveOperation(
        originalId: Long,
        type: TransactionType,
        receipt: SaleReceipt,
        amountInCents: Long,
    ): Long {
        require(type != TransactionType.SALE) { "Use saveSale for sales" }
        val original = requireNotNull(getTransaction(originalId)) { "Sale $originalId not found" }
        // The operation receipt may not repeat the sale references: keep the sale's
        return dao.insert(
            receipt.toEntity(type, amountInCents, tipInCents = 0, originalId = originalId).copy(
                currency = original.currency,
                uniqueReference = receipt.uniqueReference.ifBlank { original.uniqueReference },
                maskedPan = receipt.maskedPan.ifBlank { original.maskedPan },
                cardBrand = receipt.cardBrand.ifBlank { original.cardBrand },
            ),
        )
    }

    private fun SaleReceipt.toEntity(type: TransactionType, amountInCents: Long, tipInCents: Long, originalId: Long?) =
        TransactionEntity(
            type = type.name,
            amountInCents = amountInCents,
            tipInCents = tipInCents,
            currency = currencyOf(this).name,
            authorizationCode = authorizationCode,
            reference = reference,
            uniqueReference = uniqueReference,
            transToken = raw[TRANS_TOKEN]?.takeIf { it.isNotBlank() },
            maskedPan = maskedPan,
            cardBrand = cardBrand,
            createdAt = now(),
            originalId = originalId,
        )

    private companion object {
        const val TRANS_TOKEN = "TRANS_TOKEN"

        /** CURRENCY_CODE comes as "484" / "0484"; pesos if unknown. */
        fun currencyOf(receipt: SaleReceipt): SaleCurrency =
            when (receipt.raw["CURRENCY_CODE"].orEmpty().trimStart('0')) {
                "840" -> SaleCurrency.USD
                else -> SaleCurrency.MXN
            }
    }
}

/** Entities to records, attaching to each sale the operations made on it. */
internal fun List<TransactionEntity>.toRecords(): List<TransactionRecord> {
    val records = map { it.toRecord() }
    val operationsBySale = records.filter { it.originalId != null }.groupBy { it.originalId }
    return records.map { record ->
        if (record.type == TransactionType.SALE) record.copy(operations = operationsBySale[record.id].orEmpty()) else record
    }
}

private fun TransactionEntity.toRecord() = TransactionRecord(
    id = id,
    type = TransactionType.entries.firstOrNull { it.name == type } ?: TransactionType.SALE,
    amountInCents = amountInCents,
    tipInCents = tipInCents,
    currency = currency,
    authorizationCode = authorizationCode,
    reference = reference,
    uniqueReference = uniqueReference,
    transToken = transToken,
    maskedPan = maskedPan,
    cardBrand = cardBrand,
    createdAt = createdAt,
    originalId = originalId,
)
