package com.smart.smartpay.domain.models

object ErrorCodes {
    /** [com.smart.smartpay.core.PaymentSdkManager.initialize] was not called. */
    const val NOT_INITIALIZED: String = "SDK_NOT_INITIALIZED"

    /** [com.smart.smartpay.core.PaymentSdkManager.requiredPermissions] are not granted. */
    const val PERMISSIONS_REQUIRED: String = "SDK_PERMISSIONS_REQUIRED"

    /** Authenticated, but storing the configuration or initializing the EMV module failed. */
    const val TERMINAL_SETUP_FAILED: String = "SDK_TERMINAL_SETUP_FAILED"

    /** The authentication finished without reporting any result. */
    const val NO_RESPONSE: String = "SDK_NO_RESPONSE"
}