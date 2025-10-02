/*
 * Base Ads Sample - Custom Hilt Modules
 * 
 * Custom Hilt modules for the sample app to override default
 * Base Ads implementations with app-specific ones.
 */

package com.tinhtx.baseads.di

import com.tinhtx.baseads.BuildConfig
import com.tinhtx.baseads.core.AdUnitsProvider
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.ProductionAdUnitsProvider
import com.tinhtx.baseads.core.TestVipGate
import com.tinhtx.baseads.core.VipGate
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Sample app's custom module for overriding specific Base Ads implementations
 * This module only provides components that need to be different from defaults
 */
@Module
@InstallIn(SingletonComponent::class)
object SampleAppAdsModule {
    
    /**
     * Provides custom AdsConfig with ironSource configuration for sample app
     * Shows how to configure ironSource app key in your app
     */
    @Provides
    @Singleton
    fun provideSampleAdsConfig(): AdsConfig {
        return AdsConfig(
            enableAds = true,
            enableInterstitial = true,
            enableBanner = true,
            interstitialBlocklistRoutes = setOf(
                "premium", "vip", "subscription",
                "auth", "login", "register",
                "checkout", "payment", "purchase", "billing",
                "settings" // Added for sample app
            ),
            showInterstitialBeforeNavigate = false,
            
            // 🎯 ironSource configuration
            ironSourceAppKey = "23b463c45", // ironSource app key
            enableIronSourceLogging = BuildConfig.DEBUG
        )
    }
    
    /**
     * Provides custom AdUnitsProvider for the sample app
     * Shows how to configure production AdMob ad unit IDs in your app
     * 
     * NOTE: Replace with your actual AdMob ad unit IDs for production
     */
    @Provides
    @Singleton
    fun provideSampleAdUnitsProvider(): AdUnitsProvider {
        return if (BuildConfig.DEBUG) {
            // Use Google Test Ad Unit IDs for DEBUG builds
            ProductionAdUnitsProvider(
                // 🧪 Google Test Ad Unit IDs (always work in debug)
                bannerUnitId = "ca-app-pub-3940256099942544/6300978111",
                interstitialUnitId = "ca-app-pub-3940256099942544/1033173712",
            )
        } else {
            // Use your REAL AdMob ad units for RELEASE builds
            ProductionAdUnitsProvider(
                // 🎯 Your REAL AdMob Ad Unit IDs for production
                bannerUnitId = "ca-app-pub-8819120490234533/8043624743", // Your banner ad unit ID
                interstitialUnitId = "ca-app-pub-8819120490234533/XXXXXXXXXX", // Replace with your real interstitial ID
            )
        }
    }
    
    /**
     * Provides TestVipGate for the sample app to allow VIP testing
     * This overrides the default VIP gate to provide a testable implementation
     */
    @Provides
    @Singleton
    fun provideSampleVipGate(): VipGate {
        return TestVipGate(vipStatus = false) // Start as non-VIP
    }
}