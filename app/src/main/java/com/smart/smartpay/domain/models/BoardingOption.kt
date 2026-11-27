package com.smart.smartpay.domain.models

/** An onboarding the user can choose from. */
data class BoardingOption(
    val id: String,
    val name: String,
    val description: String,
)