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
        adsPrefs.incrementAppLaunchCount()
        adsInitializer.initializeProductionMode(context = this)
        forceUpdateRepository.init()
    }
}