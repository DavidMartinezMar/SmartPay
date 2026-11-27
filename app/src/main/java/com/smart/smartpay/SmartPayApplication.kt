package com.smart.smartpay

import android.app.Application
import com.smart.smartpay.core.PaymentSdkManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartPayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PaymentSdkManager.initialize(this)
    }
}
