package com.smart.smartpay.domain.utils.extensionfunctions

import com.smart.core_emv_sale.domain.models.PromotionSelected
import com.smart.smartpay.domain.models.PromotionChoice

fun PromotionChoice.toInternal(): PromotionSelected = when (this) {
    PromotionChoice.None -> PromotionSelected.WithoutPromotions
    PromotionChoice.LoyaltyPayment -> PromotionSelected.LoyaltyPayment
    is PromotionChoice.MonthsWithoutInterest -> PromotionSelected.MonthsWithoutInterest(months.toString())
    is PromotionChoice.MonthsWithInterest -> PromotionSelected.MonthsWithInterest(months.toString())
}