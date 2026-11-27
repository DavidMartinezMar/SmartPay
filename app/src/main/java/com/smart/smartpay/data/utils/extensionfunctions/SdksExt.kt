package com.smart.smartpay.data.utils.extensionfunctions

import com.smart.core_emv_sale.domain.models.DccOfferAvailable
import com.smart.core_emv_sale.domain.models.IntercardData
import com.smart.core_emv_sale.domain.models.PromotionsAvailable
import com.smart.smartpay.domain.models.DccOffer
import com.smart.smartpay.domain.models.Intercard
import com.smart.smartpay.domain.models.PromotionOptions
import kotlin.collections.mapNotNull
import kotlin.collections.orEmpty
import kotlin.text.toIntOrNull

fun DccOfferAvailable.toPublic(): DccOffer = DccOffer(
    localAmount = localAmount,
    exchangeRate = exchangeRate,
    mxnVsCurrency = mxnVsCurrency,
    markUp = markUp,
    foreignAmount = foreignAmount,
    foreignCurrency = foreignCurrency,
    reference = reference,
)

fun IntercardData.toPublic(): Intercard = Intercard(
    uid = uid,
    atqa = atqa,
    sak = sak,
    siteCode = siteCode,
    technology = technology,
    accountNumber = accountNumber,
    encryptedAccountNumber = encryptedAccountNumber
)

fun PromotionsAvailable.toPublic(): PromotionOptions = PromotionOptions(
    monthsWithoutInterest = monthsWithoutInterest.orEmpty().mapNotNull { it.toIntOrNull() },
    monthsWithInterest = monthsWithInterest.orEmpty().mapNotNull { it.toIntOrNull() },
    loyaltyPaymentAvailable = acceptLoyaltyPayment,
)
