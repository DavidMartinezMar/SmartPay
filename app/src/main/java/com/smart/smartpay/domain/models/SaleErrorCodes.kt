package com.smart.smartpay.domain.models

object SaleErrorCodes {
    const val NOT_INITIALIZED: String = "SDK_NOT_INITIALIZED"

    /** The terminal was not authenticated in this process (AuthenticateProfilerUseCase). */
    const val TERMINAL_NOT_READY: String = "SDK_TERMINAL_NOT_READY"

    /** Another sale is in progress. */
    const val SALE_IN_PROGRESS: String = "SDK_SALE_IN_PROGRESS"
}