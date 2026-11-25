package com.smart.smartpay

import android.app.Application
import com.smart.smartpay.core.PaymentSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartPayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PaymentSdk.initialize(this)
    }
}
