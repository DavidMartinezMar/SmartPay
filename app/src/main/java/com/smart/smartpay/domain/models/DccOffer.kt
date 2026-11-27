package com.smart.smartpay.domain.models

/** Dynamic Currency Conversion offer: pay a foreign card in its own currency. */
data class DccOffer(
    val localAmount: String,
    val exchangeRate: String,
    val mxnVsCurrency: String,
    val markUp: String,
    val foreignAmount: String,
    val foreignCurrency: String,
    val reference: String,
)