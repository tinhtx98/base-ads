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
import com.tinhtx.baseads.forceupdate.data.ForceUpdateRepository
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
    
    @Inject
    lateinit var forceUpdateRepository: ForceUpdateRepository
    
    override fun onCreate() {
        super.onCreate()
        
        // Increment app launch count for analytics
        adsPrefs.incrementAppLaunchCount()
        
        // Initialize ads based on build type
        if (BuildConfig.DEBUG) {
            // DEBUG BUILD: Use test mode for safe extensive testing
            // ⚠️ Note: Bidding networks (ironSource, Vungle, Meta) may not return bids in test mode
            adsInitializer.initializeWithTestDevices(
                context = this,
                includeCommonTestDevices = true
            )
        } else {
            // RELEASE BUILD: Use production mode for real bidding
            // ✅ All networks will participate in bidding and return real eCPMs
            adsInitializer.initializeProductionMode(context = this)
        }
        
        // MANUAL OVERRIDE for testing real bidding in debug:
        // Uncomment below to test production bidding behavior:
        // adsInitializer.initializeProductionMode(context = this)
        
        // Initialize force update with remote config
        forceUpdateRepository.init()
    }
}