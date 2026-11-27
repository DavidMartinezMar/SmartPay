package com.smart.smartpay.domain.models

enum class DccChoice {
    /** Pay in the card's currency ([DccOffer.foreignAmount]). */
    FOREIGN_CURRENCY,
    /** Pay in pesos. */
    LOCAL_CURRENCY,
}
