package com.smart.smartpay.data.payment

import com.smart.smartpay.domain.payment.SaleCurrency
import com.smart.smartpay.domain.payment.SaleReceipt

/** Builds a [SaleReceipt] from the transaction fields (common's collectionEmvParameters). */
internal fun Map<String, String>.toReceipt(): SaleReceipt {
    fun field(key: String) = this[key].orEmpty()
    return SaleReceipt(
        authorizationCode = field("TXN_APPROVAL_CODE"),
        reference = this["SG_REFERENCE"]?.takeIf { it.isNotBlank() } ?: field("REFERENCIA"),
        uniqueReference = field("TXN_UNIQUE_REF"),
        maskedPan = field("ACCOUNT_NUMBER"),
        cardHolderName = field("CARD_HOLDER_NAME").trim(),
        cardBrand = field("BRAND_NAME"),
        cardType = field("CARD_TYPE"),
        bankName = field("BANK_NAME"),
        applicationLabel = field("APP_LABEL"),
        aid = field("AID"),
        arqc = field("ARQC"),
        entryMode = field("ENTRY_MODE"),
        date = field("TXN_DATE"),
        time = field("TXN_TIME"),
        folio = field("FOLIO_NUMBER"),
        merchantId = field("MERCHANT_ID"),
        terminalId = this["TERMINAL_ID"]?.takeIf { it.isNotBlank() } ?: field("POS_ID"),
        signatureRequired = field("SIGNATURE_FLAG") == "1",
        headers = (1..4).map { field("HEADER_$it") }.filter { it.isNotBlank() },
        footers = (1..4).map { field("FOOTER_$it") }.filter { it.isNotBlank() },
        raw = toMap(),
    )
}

/** Currency of the transaction of [receipt] (CURRENCY_CODE, "484" or "0484" style); pesos if unknown. */
internal fun currencyOf(receipt: SaleReceipt): SaleCurrency {
    val code = receipt.raw["CURRENCY_CODE"].orEmpty().trimStart('0')
    return SaleCurrency.entries.firstOrNull { it.code == code } ?: SaleCurrency.MXN
}
