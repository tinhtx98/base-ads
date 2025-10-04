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
    private val adsConfig: AdsConfig,
    private val vungleBiddingManager: com.tinhtx.baseads.mediation.VungleBiddingManager,
    private val ironSourceBiddingManager: com.tinhtx.baseads.mediation.IronSourceBiddingManager
) {
    
    private var isInitialized = false
    private var initializationCallbacks = mutableListOf<() -> Unit>()
    
    /**
     * Adds a callback to be executed when ads initialization is complete
     */
    fun onInitialized(callback: () -> Unit) {
        if (isInitialized) {
            callback()
        } else {
            initializationCallbacks.add(callback)
        }
    }
    
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
                // Note: With bidding mediation, no manual SDK initialization needed
                // All mediation is handled automatically by Google Mobile Ads SDK
                AdsLogger.i("Initializer", "Using bidding mediation - automatic configuration")
                
                // Initialize bidding analytics for mediation partners
                initializeBiddingPartners()
                
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
                    
                    // Execute pending callbacks
                    initializationCallbacks.forEach { it() }
                    initializationCallbacks.clear()
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
     * Initializes bidding mediation partners analytics
     */
    private fun initializeBiddingPartners() {
        try {
            AdsLogger.i("Initializer", "Initializing bidding mediation partners")
            
            // Initialize bidding partner analytics
            vungleBiddingManager.initialize()
            ironSourceBiddingManager.initialize()
            
            AdsLogger.i("Initializer", "Bidding partners initialization completed")
            
        } catch (e: Exception) {
            AdsLogger.e("Initializer", "Failed to initialize bidding partners", e)
        }
    }
}