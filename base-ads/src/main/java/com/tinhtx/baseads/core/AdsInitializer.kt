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
    private val ironSourceBiddingManager: com.tinhtx.baseads.mediation.IronSourceBiddingManager,
    private val metaBiddingManager: com.tinhtx.baseads.mediation.MetaBiddingManager
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
                    AdsLogger.i("Initializer", "═══════════════════════════════════════")
                    AdsLogger.i("Initializer", "📊 ADAPTER STATUS REPORT")
                    AdsLogger.i("Initializer", "═══════════════════════════════════════")
                    
                    var readyCount = 0
                    var notReadyCount = 0
                    
                    // Log adapter statuses with detailed info
                    statusMap.forEach { (adapterName, status) ->
                        val state = status.initializationState.name
                        val description = status.description
                        val latency = status.latency
                        
                        when (status.initializationState.name) {
                            "READY" -> {
                                readyCount++
                                AdsLogger.i(
                                    "Initializer",
                                    "✅ $adapterName: READY (${latency}ms)"
                                )
                            }
                            "NOT_READY" -> {
                                notReadyCount++
                                AdsLogger.e(
                                    "Initializer",
                                    "❌ $adapterName: NOT_READY - $description"
                                )
                                // Log to analytics for monitoring
                                analyticsLogger.logEvent("adapter_not_ready", mapOf(
                                    "adapter" to adapterName,
                                    "reason" to description,
                                    "latency" to latency
                                ))
                            }
                            else -> {
                                AdsLogger.w(
                                    "Initializer",
                                    "⚠️ $adapterName: $state - $description"
                                )
                            }
                        }
                    }
                    
                    AdsLogger.i("Initializer", "═══════════════════════════════════════")
                    AdsLogger.i("Initializer", "Summary: $readyCount ready, $notReadyCount not ready")
                    AdsLogger.i("Initializer", "═══════════════════════════════════════")
                    
                    // Log analytics event
                    analyticsLogger.logEvent("ads_init", mapOf(
                        "adapters_count" to statusMap.size,
                        "adapters_ready" to readyCount,
                        "adapters_not_ready" to notReadyCount,
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
            emptyList()
        } else {
            emptyList()
        }
        
        AdsLogger.w("Initializer", "🧪 TEST MODE: Using test device IDs - bidding networks may not return bids")
        initialize(context, testDevices)
    }
    
    /**
     * Initialize in production mode (no test devices)
     * Use this to test real bidding behavior from all networks.
     * 
     * ⚠️ WARNING: This will serve real ads. Use sparingly during testing.
     * Excessive clicking on your own ads violates AdMob policies.
     */
    fun initializeProductionMode(context: Context) {
        AdsLogger.w("Initializer", "🚀 PRODUCTION MODE: Real ads will be served from all networks!")
        AdsLogger.w("Initializer", "⚠️ Bidding networks (ironSource, Vungle, Meta) will return real bids")
        initialize(context, testDeviceIds = emptyList())
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
            metaBiddingManager.initialize()
            
            AdsLogger.i("Initializer", "Bidding partners initialization completed")
            
        } catch (e: Exception) {
            AdsLogger.e("Initializer", "Failed to initialize bidding partners", e)
        }
    }
}