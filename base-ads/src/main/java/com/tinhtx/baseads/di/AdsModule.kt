/*
 * Base Ads Module - Dependency Injection Module
 * 
 * Hilt module providing default implementations for all ads-related dependencies.
 * Apps can override these bindings to provide custom implementations.
 */

package com.tinhtx.baseads.di

import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.core.FirebaseAnalyticsLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for ads dependency injection.
 * Provides default implementations that can be overridden by apps.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AdsModule {

    /**
     * Binds the default analytics logger implementation
     */
    @Binds
    @Singleton
    abstract fun bindAnalyticsLogger(
        firebaseAnalyticsLogger: FirebaseAnalyticsLogger
    ): AnalyticsLogger

    // Note: AdUnitsProvider should be provided by the app module
    // to allow custom ad unit ID configuration. See SampleAppAdsModule for example.

    companion object {

        // Note: AdsConfig should be provided by the app module
        // to allow custom configuration. See SampleAppAdsModule for example.
    }
}