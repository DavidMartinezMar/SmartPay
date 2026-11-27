package com.smart.smartpay.domain.utils.extensionfunctions

import com.smart.core_emv_sale.domain.models.DccOfferSelected
import com.smart.smartpay.domain.models.DccChoice

fun DccChoice.toInternal(): DccOfferSelected = when (this) {
    DccChoice.FOREIGN_CURRENCY -> DccOfferSelected.DccOfferForeignSelected
    DccChoice.LOCAL_CURRENCY -> DccOfferSelected.DccOfferLocalSelected
}