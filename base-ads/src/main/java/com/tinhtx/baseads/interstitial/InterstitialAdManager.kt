/*
 * Base Ads Module - Interstitial Ad Manager
 * 
 * Manages interstitial ads lifecycle including preloading, policy checks,
 * and showing ads with proper cooldown and safety measures.
 */

package com.tinhtx.baseads.interstitial

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.AdValue
import com.tinhtx.baseads.core.AdUnitsProvider
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdsConstants
import com.tinhtx.baseads.core.AdsLogger
import com.tinhtx.baseads.core.AdStatus
import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.core.VipGate
import com.tinhtx.baseads.data.AdsPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manages interstitial ads with smart show policies and safety measures.
 * Handles preloading, cooldown, daily caps, and route blocking.
 */
@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnitsProvider: AdUnitsProvider,
    private val adsConfig: AdsConfig,
    private val vipGate: VipGate,
    private val analyticsLogger: AnalyticsLogger,
    private val adsPrefs: AdsPrefs,
    private val adRevenueReporter: com.tinhtx.baseads.core.AdRevenueReporter
) {
    
    private var cachedAd: InterstitialAd? = null
    private var isLoading = false
    private val lastScreenOpenAtMs = AtomicLong(0)
    @Volatile private var lastShowRoute: String? = null
    
    private val interstitialAdId = "interstitial_main"  // Unique ID for interstitial tracking
    
    companion object {
        private val appLaunchMs = System.currentTimeMillis()
    }
    
    /**
     * Marks that a screen was opened. Should be called when navigating to any screen.
     * Used to enforce minimum time before showing interstitial.
     */
    fun markScreenOpened() {
        lastScreenOpenAtMs.set(System.currentTimeMillis())
        AdsLogger.d("Interstitial", "Screen opened marked at ${System.currentTimeMillis()}")
    }
    
    /**
     * Preloads an interstitial ad if conditions are met.
     * Should be called proactively to ensure ads are ready when needed.
     */
    fun preload() {
        // Don't preload if already have cached ad or currently loading
        if (cachedAd != null || isLoading) {
            AdsLogger.d("Interstitial", "Skipping preload - already cached or loading")
            return
        }
        
        // Don't preload if ads are disabled
        if (vipGate.isVip() || !adsConfig.shouldShowInterstitial()) {
            AdsLogger.registerAd(interstitialAdId, "interstitial")
            AdsLogger.updateAdStatus(interstitialAdId, AdStatus.NOT_AVAILABLE,
                if (vipGate.isVip()) "VIP user" else "Interstitial disabled")
            AdsLogger.d("Interstitial", "Skipping preload - ads disabled or VIP")
            return
        }
        
        AdsLogger.registerAd(interstitialAdId, "interstitial")
        AdsLogger.updateAdStatus(interstitialAdId, AdStatus.INITIALIZING)
        AdsLogger.d("Interstitial", "Starting interstitial preload")
        AdsLogger.markInterstitialRequest()
        
        isLoading = true
        
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            adUnitsProvider.interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    cachedAd = interstitialAd
                    isLoading = false

                    // Attach ILRD listener (Impression Level Revenue Data)
                    interstitialAd.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                        // Get mediation adapter info for proper attribution
                        val responseInfo = interstitialAd.responseInfo
                        val adapterClassName = responseInfo?.loadedAdapterResponseInfo?.adapterClassName
                        
                        adRevenueReporter.reportInterstitialRevenue(
                            adUnitsProvider.interstitialAdUnitId,
                            adValue.valueMicros,
                            adValue.currencyCode,
                            adValue.precisionType,
                            adapterClassName = adapterClassName,
                            route = lastShowRoute
                        )
                    }
                    
                    AdsLogger.updateAdStatus(interstitialAdId, AdStatus.READY)
                    AdsLogger.d("Interstitial", "Interstitial ad loaded successfully")
                    
                    analyticsLogger.logEvent("ad_interstitial_loaded", mapOf(
                        "ad_unit_id" to adUnitsProvider.interstitialAdUnitId
                    ))
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoading = false
                    
                    val errorMsg = "Code: ${loadAdError.code} - ${loadAdError.message}"
                    AdsLogger.updateAdStatus(interstitialAdId, AdStatus.FAILED, errorMsg)
                    AdsLogger.e(
                        "Interstitial",
                        "Failed to load interstitial ad. $errorMsg, Domain: ${loadAdError.domain}"
                    )
                    
                    analyticsLogger.logEvent("ad_interstitial_load_failed", mapOf(
                        "error_code" to loadAdError.code,
                        "error_message" to loadAdError.message,
                        "error_domain" to loadAdError.domain,
                        "ad_unit_id" to adUnitsProvider.interstitialAdUnitId
                    ))
                }
            }
        )
    }
    
    /**
     * Attempts to show an interstitial ad if policy conditions are met.
     * 
     * @param activity Current activity to show the ad
     * @param currentRoute Current route/screen name (for blocklist checking)
     * @param onShown Optional callback when ad is shown/dismissed
     * @return true if ad was shown, false otherwise
     */
    suspend fun maybeShow(
        activity: Activity,
        currentRoute: String? = null,
        onShown: (() -> Unit)? = null
    ): Boolean = suspendCancellableCoroutine { continuation ->
        
        val now = System.currentTimeMillis()
        
        // Check if we should show the ad based on policy
        if (!passPolicy(now, currentRoute)) {
            AdsLogger.d("Interstitial", "Policy check failed, not showing ad")
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        
        // Check if we have a cached ad
        val ad = cachedAd
        if (ad == null) {
            AdsLogger.d("Interstitial", "No cached ad available")
            // Try to preload for next time
            preload()
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        
    AdsLogger.d("Interstitial", "Showing interstitial ad")
    lastShowRoute = currentRoute
        
        // Set callback for ad events
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                AdsLogger.updateAdStatus(interstitialAdId, AdStatus.SHOWING)
                AdsLogger.d("Interstitial", "Interstitial ad impression")
                AdsLogger.markInterstitialImpression()
                
                analyticsLogger.logEvent("ad_interstitial_impression", mapOf(
                    "route" to currentRoute,
                    "ad_unit_id" to adUnitsProvider.interstitialAdUnitId
                ))
            }
            
            override fun onAdDismissedFullScreenContent() {
                AdsLogger.updateAdStatus(interstitialAdId, AdStatus.DISMISSED)
                AdsLogger.d("Interstitial", "Interstitial ad dismissed")
                
                // Clear cached ad
                cachedAd = null
                
                // Update preferences
                adsPrefs.setLastInterstitialEpoch(System.currentTimeMillis() / 1000)
                adsPrefs.incrementTodayInterstitialCount()
                
                analyticsLogger.logEvent("ad_interstitial_dismissed", mapOf(
                    "route" to currentRoute,
                    "today_count" to adsPrefs.getTodayInterstitialCount(),
                    "total_count" to adsPrefs.getTotalInterstitialShown()
                ))
                
                // Preload next ad
                preload()
                
                // Notify callback
                onShown?.invoke()
                
                // Resume coroutine with success
                if (continuation.isActive) {
                    continuation.resume(true)
                }
                
                AdsLogger.logRateSummary()
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                val errorMsg = "Code: ${adError.code} - ${adError.message}"
                AdsLogger.updateAdStatus(interstitialAdId, AdStatus.FAILED, errorMsg)
                AdsLogger.e(
                    "Interstitial",
                    "Failed to show interstitial ad. $errorMsg, Domain: ${adError.domain}"
                )
                
                // Clear cached ad
                cachedAd = null
                
                analyticsLogger.logEvent("ad_interstitial_show_failed", mapOf(
                    "error_code" to adError.code,
                    "error_message" to adError.message,
                    "error_domain" to adError.domain,
                    "route" to currentRoute
                ))
                
                // Preload next ad
                preload()
                
                // Resume coroutine with failure
                if (continuation.isActive) {
                    continuation.resume(false)
                }
            }
        }
        
        // Show the ad
        try {
            ad.show(activity)
        } catch (e: Exception) {
            AdsLogger.e("Interstitial", "Exception showing interstitial ad", e)
            
            // Clear cached ad
            cachedAd = null
            
            // Preload next ad
            preload()
            
            // Resume coroutine with failure
            if (continuation.isActive) {
                continuation.resume(false)
            }
        }
    }
    
    /**
     * Force shows an interstitial ad immediately, bypassing all policy checks.
     * This method ignores VIP status, cooldowns, daily caps, and route blocklists.
     * 
     * @param activity Current activity to show the ad
     * @param onShown Optional callback when ad is shown/dismissed
     * @return true if ad was shown, false if no ad available
     */
    fun show(
        activity: Activity,
        onShown: (() -> Unit)? = null
    ): Boolean {
        AdsLogger.d("Interstitial", "Force showing interstitial ad (bypassing policy)")
        
        // Check if we have a cached ad
        val ad = cachedAd
        if (ad == null) {
            AdsLogger.d("Interstitial", "No cached ad available for force show")
            // Try to preload for next time
            preload()
            return false
        }
        
    // Set callback for ad events
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                AdsLogger.updateAdStatus(interstitialAdId, AdStatus.SHOWING)
                AdsLogger.d("Interstitial", "Force interstitial ad impression")
                AdsLogger.markInterstitialImpression()
                
                analyticsLogger.logEvent("ad_interstitial_impression", mapOf(
                    "type" to "force_show",
                    "ad_unit_id" to adUnitsProvider.interstitialAdUnitId
                ))
            }
            
            override fun onAdDismissedFullScreenContent() {
                AdsLogger.updateAdStatus(interstitialAdId, AdStatus.DISMISSED)
                AdsLogger.d("Interstitial", "Force interstitial ad dismissed")
                
                // Clear cached ad
                cachedAd = null
                
                // Update preferences (even for force show)
                adsPrefs.setLastInterstitialEpoch(System.currentTimeMillis() / 1000)
                adsPrefs.incrementTodayInterstitialCount()
                
                analyticsLogger.logEvent("ad_interstitial_dismissed", mapOf(
                    "type" to "force_show",
                    "today_count" to adsPrefs.getTodayInterstitialCount(),
                    "total_count" to adsPrefs.getTotalInterstitialShown()
                ))
                
                // Preload next ad
                preload()
                
                // Notify callback
                onShown?.invoke()
                
                AdsLogger.logRateSummary()
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                val errorMsg = "Code: ${adError.code} - ${adError.message}"
                AdsLogger.updateAdStatus(interstitialAdId, AdStatus.FAILED, errorMsg)
                AdsLogger.e(
                    "Interstitial",
                    "Failed to show force interstitial ad. $errorMsg, Domain: ${adError.domain}"
                )
                
                // Clear cached ad
                cachedAd = null
                
                analyticsLogger.logEvent("ad_interstitial_show_failed", mapOf(
                    "error_code" to adError.code,
                    "error_message" to adError.message,
                    "error_domain" to adError.domain,
                    "type" to "force_show"
                ))
                
                // Preload next ad
                preload()
            }
        }
        
        // Show the ad
        lastShowRoute = null // force show has no contextual route
        return try {
            ad.show(activity)
            true
        } catch (e: Exception) {
            AdsLogger.e("Interstitial", "Exception force showing interstitial ad", e)
            
            // Clear cached ad
            cachedAd = null
            
            // Preload next ad
            preload()
            
            false
        }
    }
    
    /**
     * Checks if the ad passes policy requirements for showing
     */
    private fun passPolicy(nowMs: Long, route: String?): Boolean {
        // Check if ads are enabled
        if (vipGate.isVip() || !adsConfig.shouldShowInterstitial()) {
            AdsLogger.d("Policy", "Ads disabled or VIP status")
            return false
        }
        
        // Check route blocklist
        if (adsConfig.isRouteBlocked(route)) {
            AdsLogger.d("Policy", "Route '$route' is blocked")
            return false
        }
        
        // Check minimum time since screen open
        val sinceScreenOpenMs = nowMs - lastScreenOpenAtMs.get()
        val minScreenOpenMs = AdsConstants.MIN_SECONDS_AFTER_SCREEN_OPEN * 1000
        if (sinceScreenOpenMs < minScreenOpenMs) {
            AdsLogger.d(
                "Policy", 
                "Not enough time since screen open: ${sinceScreenOpenMs}ms < ${minScreenOpenMs}ms"
            )
            return false
        }
        
        // Check cooldown since last interstitial
        val lastInterstitialEpoch = adsPrefs.getLastInterstitialEpoch()
        val nowEpoch = nowMs / 1000
        val elapsedSinceLastMs = (nowEpoch - lastInterstitialEpoch)
        if (elapsedSinceLastMs < AdsConstants.INTERSTITIAL_COOLDOWN_SECONDS) {
            AdsLogger.d(
                "Policy", 
                "Cooldown not met: ${elapsedSinceLastMs}s < ${AdsConstants.INTERSTITIAL_COOLDOWN_SECONDS}s"
            )
            return false
        }
        
        // Check daily cap
        val todayCount = adsPrefs.getTodayInterstitialCount()
        if (todayCount >= AdsConstants.INTERSTITIAL_DAILY_CAP) {
            AdsLogger.d("Policy", "Daily cap reached: $todayCount >= ${AdsConstants.INTERSTITIAL_DAILY_CAP}")
            return false
        }
        
        // Check first launch delay
        val sinceAppLaunchMs = nowMs - appLaunchMs
        val firstLaunchDelayMs = AdsConstants.INTERSTITIAL_FIRST_LAUNCH_DELAY_SECONDS * 1000
        if (sinceAppLaunchMs < firstLaunchDelayMs) {
            AdsLogger.d(
                "Policy", 
                "First launch delay not met: ${sinceAppLaunchMs}ms < ${firstLaunchDelayMs}ms"
            )
            return false
        }
        
        AdsLogger.d("Policy", "All policy checks passed")
        return true
    }
    
    /**
     * Checks if an interstitial ad is ready to be shown
     * @return true if ad is loaded and ready, false otherwise
     */
    fun isReady(): Boolean {
        return cachedAd != null && !isLoading
    }
    
    /**
     * Gets debug information about current state
     */
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "has_cached_ad" to (cachedAd != null),
            "is_loading" to isLoading,
            "last_screen_open_ms" to lastScreenOpenAtMs.get(),
            "app_launch_ms" to appLaunchMs,
            "current_time_ms" to System.currentTimeMillis(),
            "vip_status" to vipGate.isVip(),
            "config_enabled" to adsConfig.shouldShowInterstitial()
        ) + adsPrefs.getDebugInfo()
    }
    
    /**
     * Clears cached ad (useful for testing)
     */
    fun clearCache() {
        cachedAd = null
        isLoading = false
        AdsLogger.d("Interstitial", "Cache cleared")
    }
}