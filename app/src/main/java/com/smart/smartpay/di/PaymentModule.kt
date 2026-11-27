package com.smart.smartpay.di

import com.smart.smartpay.domain.authentication.AuthenticateProfilerUseCase
import com.smart.smartpay.domain.payment.SaleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Exposes the payment use cases to Hilt (the domain layer doesn't depend on Hilt). */
@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {

    @Provides
    fun provideAuthenticateProfilerUseCase(): AuthenticateProfilerUseCase = AuthenticateProfilerUseCase()

    @Provides
    fun provideSaleUseCase(): SaleUseCase = SaleUseCase()
}
