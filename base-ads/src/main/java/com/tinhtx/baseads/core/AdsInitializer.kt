/*
 * Base Ads Module - Ads Initializer
 * 
 * Handles initialization of Google Mobile Ads SDK and analytics logging.
 * Should be called during application startup.
 */

package com.tinhtx.baseads.core

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.ironsource.mediationsdk.IronSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Initializes the ads module and Google Mobile Ads SDK.
 * Handles test device configuration and analytics logging.
 */
@Singleton
class AdsInitializer @Inject constructor(
    private val analyticsLogger: AnalyticsLogger,
    private val adsConfig: AdsConfig
) {
    
    private var isInitialized = false
    
    /**
     * Initializes the Google Mobile Ads SDK
     * 
     * @param context Application context
     * @param testDeviceIds Optional list of test device IDs for development
     */
    fun initialize(
        context: Context, 
        testDeviceIds: List<String> = emptyList()
    ) {
        if (isInitialized) {
            AdsLogger.w("Initializer", "Ads already initialized, skipping")
            return
        }
        
        AdsLogger.i("Initializer", "Initializing Google Mobile Ads SDK")
        
        // Initialize on background thread to avoid blocking main thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Initialize ironSource if configured
                initializeIronSourceIfNeeded(context)
                
                // Configure test devices if provided
                if (testDeviceIds.isNotEmpty()) {
                    val requestConfiguration = RequestConfiguration.Builder()
                        .setTestDeviceIds(testDeviceIds)
                        .build()
                    MobileAds.setRequestConfiguration(requestConfiguration)
                    
                    AdsLogger.d("Initializer", "Test devices configured: $testDeviceIds")
                }
                
                // Initialize MobileAds SDK
                MobileAds.initialize(context) { initializationStatus ->
                    val statusMap = initializationStatus.adapterStatusMap
                    
                    AdsLogger.i("Initializer", "MobileAds initialization completed")
                    
                    // Log adapter statuses
                    statusMap.forEach { (adapterName, status) ->
                        AdsLogger.d(
                            "Initializer", 
                            "Adapter: $adapterName, Status: ${status.initializationState}, " +
                            "Description: ${status.description}"
                        )
                    }
                    
                    // Log analytics event
                    analyticsLogger.logEvent("ads_init", mapOf(
                        "adapters_count" to statusMap.size,
                        "initialization_status" to "completed"
                    ))
                    
                    isInitialized = true
                    AdsLogger.logRateSummary()
                }
                
            } catch (e: Exception) {
                AdsLogger.e("Initializer", "Failed to initialize MobileAds", e)
                
                // Log failed initialization
                analyticsLogger.logEvent("ads_init", mapOf(
                    "initialization_status" to "failed",
                    "error" to e.message
                ))
            }
        }
    }
    
    /**
     * Checks if the ads module has been initialized
     */
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * Convenience method to initialize with common test device configurations
     */
    fun initializeWithTestDevices(
        context: Context,
        includeCommonTestDevices: Boolean = true
    ) {
        val testDevices = if (includeCommonTestDevices) {
            listOf(
                AdRequest.DEVICE_ID_EMULATOR, // Use AdMob's emulator constant
                "33BE2250B43518CCDA7DE426D04EE231" // Common test device (replace with your device ID)
            )
        } else {
            emptyList()
        }
        
        initialize(context, testDevices)
    }
    
    /**
     * Initializes ironSource SDK if app key is configured
     */
    private fun initializeIronSourceIfNeeded(context: Context) {
        val appKey = adsConfig.getIronSourceAppKeySafe()
        if (appKey != null) {
            try {
                AdsLogger.i("Initializer", "Initializing ironSource with app key: ${appKey.take(8)}...")
                
                // Enable logging if configured
                if (adsConfig.enableIronSourceLogging) {
                    IronSource.setAdaptersDebug(true)
                    IronSource.shouldTrackNetworkState(context, true)
                    AdsLogger.d("Initializer", "ironSource debug logging enabled")
                }
                
                // Initialize ironSource SDK
                IronSource.init(context, appKey, IronSource.AD_UNIT.BANNER, IronSource.AD_UNIT.INTERSTITIAL)
                
                AdsLogger.i("Initializer", "ironSource initialization completed")
                
                // Log analytics event
                analyticsLogger.logEvent("ironsource_init", mapOf(
                    "app_key_prefix" to appKey.take(8),
                    "logging_enabled" to adsConfig.enableIronSourceLogging
                ))
                
            } catch (e: Exception) {
                AdsLogger.e("Initializer", "Failed to initialize ironSource", e)
                
                // Log failed initialization
                analyticsLogger.logEvent("ironsource_init", mapOf(
                    "initialization_status" to "failed",
                    "error" to e.message
                ))
            }
        } else {
            AdsLogger.d("Initializer", "ironSource app key not configured, skipping initialization")
        }
    }
}