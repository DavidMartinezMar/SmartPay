package com.smart.smartpay.core

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.smart.core.SmartCore
import com.smart.core_emv.di.CoreEmvModule
import com.smart.core_payments.di.CorePaymentModule
import com.smart.smartpay.core.smartpaymentdependencies.DeviceLedController
import com.smart.smartpay.core.smartpaymentdependencies.EmvParams.buildEmvParams
import com.smart.smartpay.core.smartpaymentdependencies.SerialConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Bootstrap of the payment libraries (core-network, core-payments, core-emv, core-emv-sale).
 *
 * 1. Call [initialize] once, from `Application.onCreate()`.
 * 2. Make sure [requiredPermissions] are granted (request them at runtime) before authenticating.
 * 3. Authenticate the terminal ([com.smart.smartpay.domain.authentication.AuthenticateProfilerUseCase]);
 *    after it succeeds the terminal is ready to sell ([com.smart.smartpay.domain.payment.SaleUseCase]).
 */
object PaymentSdkManager {
    @Volatile
    internal var isInitialized: Boolean = false
        private set
    internal lateinit var applicationContext: Context
        private set
    internal val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    /** Runtime permissions needed. Empty when nothing has to be requested on this device. */
    val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            arrayOf(
                //Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            emptyArray()
        }

    /** True when all [requiredPermissions] are granted. */
    fun hasRequiredPermissions(context: Context): Boolean =
        requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    /** Initializes the payment libraries. Safe to call more than once (later calls are ignored). */
    @Synchronized
    fun initialize(application: Application) {
        if (isInitialized) return
        applicationContext = application.applicationContext
        SmartCore.initialize(application)
        CorePaymentModule.init(application, SerialConnection, DeviceLedController)
        CoreEmvModule.init(::buildEmvParams)
        isInitialized = true
    }
}
