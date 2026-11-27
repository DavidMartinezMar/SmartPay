package com.smart.smartpay.data.storage.repositories

import com.smart.smartpay.data.storage.local.TransactionDao
import com.smart.smartpay.data.storage.model.TransactionRecord
import com.smart.smartpay.data.storage.model.TransactionType
import com.smart.smartpay.data.utils.extensionfunctions.toRecords
import com.smart.smartpay.domain.models.SaleReceipt
import com.smart.smartpay.domain.utils.extensionfunctions.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

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
        dao.insert(receipt.toEntity(TransactionType.SALE, amountInCents, tipInCents, originalId = null, now = now))

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
            receipt.toEntity(type, amountInCents, tipInCents = 0, originalId = originalId, now = now).copy(
                currency = original.currency,
                uniqueReference = receipt.uniqueReference.ifBlank { original.uniqueReference },
                maskedPan = receipt.maskedPan.ifBlank { original.maskedPan },
                cardBrand = receipt.cardBrand.ifBlank { original.cardBrand },
            ),
        )
    }
}
