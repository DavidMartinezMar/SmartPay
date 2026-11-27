package com.smart.smartpay.domain.models

data class Intercard(
    val uid: String,
    val sak: String,
    val atqa: String,
    val siteCode: String,
    val technology: String,
    val accountNumber: String,
    val encryptedAccountNumber: String
)
