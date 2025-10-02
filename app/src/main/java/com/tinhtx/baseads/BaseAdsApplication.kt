/*
 * Base Ads Sample Application
 * 
 * Sample application demonstrating integration of Base Ads module
 * with Hilt dependency injection and ads initialization.
 */

package com.tinhtx.baseads

import android.app.Application
import com.tinhtx.baseads.core.AdsInitializer
import com.tinhtx.baseads.data.AdsPrefs
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Sample application class demonstrating Base Ads integration
 */
@HiltAndroidApp
class BaseAdsApplication : Application() {
    
    @Inject
    lateinit var adsInitializer: AdsInitializer
    
    @Inject 
    lateinit var adsPrefs: AdsPrefs
    
    override fun onCreate() {
        super.onCreate()
        
        // Increment app launch count for analytics
        adsPrefs.incrementAppLaunchCount()
        
        // Initialize ads with test device IDs for development
        adsInitializer.initializeWithTestDevices(
            context = this,
            includeCommonTestDevices = true
        )
    }
}