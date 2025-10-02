/*
 * Base Ads Module - Ads Logger
 * 
 * Debug-only logging utility for tracking ads requests, impressions,
 * and calculating success rates.
 */

package com.tinhtx.baseads.core

import android.util.Log
import com.tinhtx.baseads.library.BuildConfig
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Ad status enum for tracking individual ad states
 */
enum class AdStatus {
    INITIALIZING,    // Ad is being loaded
    READY,          // Ad is loaded and ready to show
    SHOWING,        // Ad is currently being displayed
    FAILED,         // Ad failed to load
    DISMISSED,      // Ad was shown and dismissed
    NOT_AVAILABLE   // Ad is not available (VIP, disabled, etc.)
}

/**
 * Data class to hold ad instance information
 */
data class AdInstance(
    val id: String,
    val type: String,  // "banner" or "interstitial"
    var status: AdStatus,
    val createdAt: Long = System.currentTimeMillis(),
    var lastUpdated: Long = System.currentTimeMillis(),
    var errorMessage: String? = null
)

/**
 * Debug-only logger for ads module.
 * Only logs when DEBUG_LOG_ENABLED is true to avoid performance impact in production.
 */
object AdsLogger {
    
    private const val TAG = "BaseAds"
    
    /**
     * Controls whether logging is enabled.
     * Only logs in debug builds to avoid performance impact in production.
     */
    val enabled: Boolean = BuildConfig.DEBUG
    
    // Thread-safe counters for tracking ads performance
    private val bannerRequests = AtomicInteger(0)
    private val bannerImpressions = AtomicInteger(0)
    private val interstitialRequests = AtomicInteger(0)
    private val interstitialImpressions = AtomicInteger(0)
    
    // Thread-safe map to track individual ad instances
    private val adInstances = ConcurrentHashMap<String, AdInstance>()
    
    /**
     * Registers a new ad instance and sets its status to INITIALIZING
     */
    fun registerAd(id: String, type: String) {
        val instance = AdInstance(
            id = id,
            type = type,
            status = AdStatus.INITIALIZING
        )
        adInstances[id] = instance
        d("AdRegistry", "[$type] Ad '$id' registered - Status: INITIALIZING")
        logAllAdsStatus()
    }
    
    /**
     * Updates ad instance status
     */
    fun updateAdStatus(id: String, status: AdStatus, errorMessage: String? = null) {
        adInstances[id]?.let { instance ->
            instance.status = status
            instance.lastUpdated = System.currentTimeMillis()
            instance.errorMessage = errorMessage
            
            val statusEmoji = when (status) {
                AdStatus.READY -> "✅"
                AdStatus.SHOWING -> "📺"
                AdStatus.FAILED -> "❌"
                AdStatus.DISMISSED -> "✓"
                AdStatus.INITIALIZING -> "⏳"
                AdStatus.NOT_AVAILABLE -> "🚫"
            }
            
            val message = if (errorMessage != null) {
                "[$instance.type] Ad '$id' $statusEmoji Status: $status - Error: $errorMessage"
            } else {
                "[$instance.type] Ad '$id' $statusEmoji Status: $status"
            }
            
            when (status) {
                AdStatus.FAILED -> e("AdRegistry", message)
                AdStatus.READY, AdStatus.SHOWING -> i("AdRegistry", message)
                else -> d("AdRegistry", message)
            }
            
            logAllAdsStatus()
        } ?: run {
            w("AdRegistry", "Attempted to update unknown ad: $id")
        }
    }
    
    /**
     * Removes an ad instance from tracking
     */
    fun unregisterAd(id: String) {
        adInstances.remove(id)?.let {
            d("AdRegistry", "[${it.type}] Ad '$id' unregistered")
            logAllAdsStatus()
        }
    }
    
    /**
     * Gets current status of an ad
     */
    fun getAdStatus(id: String): AdStatus? {
        return adInstances[id]?.status
    }
    
    /**
     * Logs status of all currently tracked ads
     */
    fun logAllAdsStatus() {
        if (!enabled) return
        
        if (adInstances.isEmpty()) {
            d("AdRegistry", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            d("AdRegistry", "📊 No ads currently tracked")
            d("AdRegistry", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            return
        }
        
        val bannerAds = adInstances.values.filter { it.type == "banner" }
        val interstitialAds = adInstances.values.filter { it.type == "interstitial" }
        
        d("AdRegistry", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        d("AdRegistry", "📊 ADS STATUS SUMMARY (${adInstances.size} total)")
        d("AdRegistry", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        if (bannerAds.isNotEmpty()) {
            d("AdRegistry", "")
            d("AdRegistry", "🎯 BANNER ADS (${bannerAds.size}):")
            bannerAds.forEach { ad ->
                val statusEmoji = when (ad.status) {
                    AdStatus.READY -> "✅"
                    AdStatus.SHOWING -> "📺"
                    AdStatus.FAILED -> "❌"
                    AdStatus.DISMISSED -> "✓"
                    AdStatus.INITIALIZING -> "⏳"
                    AdStatus.NOT_AVAILABLE -> "🚫"
                }
                val age = (System.currentTimeMillis() - ad.createdAt) / 1000
                val statusText = if (ad.errorMessage != null) {
                    "${ad.status} - ${ad.errorMessage}"
                } else {
                    "${ad.status}"
                }
                d("AdRegistry", "  $statusEmoji ${ad.id}: $statusText (age: ${age}s)")
            }
        }
        
        if (interstitialAds.isNotEmpty()) {
            d("AdRegistry", "")
            d("AdRegistry", "📽️ INTERSTITIAL ADS (${interstitialAds.size}):")
            interstitialAds.forEach { ad ->
                val statusEmoji = when (ad.status) {
                    AdStatus.READY -> "✅"
                    AdStatus.SHOWING -> "📺"
                    AdStatus.FAILED -> "❌"
                    AdStatus.DISMISSED -> "✓"
                    AdStatus.INITIALIZING -> "⏳"
                    AdStatus.NOT_AVAILABLE -> "🚫"
                }
                val age = (System.currentTimeMillis() - ad.createdAt) / 1000
                val statusText = if (ad.errorMessage != null) {
                    "${ad.status} - ${ad.errorMessage}"
                } else {
                    "${ad.status}"
                }
                d("AdRegistry", "  $statusEmoji ${ad.id}: $statusText (age: ${age}s)")
            }
        }
        
        // Count by status
        val readyCount = adInstances.values.count { it.status == AdStatus.READY }
        val initializingCount = adInstances.values.count { it.status == AdStatus.INITIALIZING }
        val failedCount = adInstances.values.count { it.status == AdStatus.FAILED }
        
        d("AdRegistry", "")
        d("AdRegistry", "📈 STATUS BREAKDOWN:")
        d("AdRegistry", "  ✅ Ready: $readyCount")
        d("AdRegistry", "  ⏳ Initializing: $initializingCount")
        d("AdRegistry", "  ❌ Failed: $failedCount")
        d("AdRegistry", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
    
    /**
     * Gets a summary of ad statuses
     */
    fun getAdsSummary(): Map<String, Any> {
        val bannerAds = adInstances.values.filter { it.type == "banner" }
        val interstitialAds = adInstances.values.filter { it.type == "interstitial" }
        
        return mapOf(
            "total_ads" to adInstances.size,
            "banner_count" to bannerAds.size,
            "interstitial_count" to interstitialAds.size,
            "ready_count" to adInstances.values.count { it.status == AdStatus.READY },
            "initializing_count" to adInstances.values.count { it.status == AdStatus.INITIALIZING },
            "failed_count" to adInstances.values.count { it.status == AdStatus.FAILED },
            "showing_count" to adInstances.values.count { it.status == AdStatus.SHOWING }
        )
    }
    
    /**
     * Logs debug message
     */
    fun d(tag: String, message: String) {
        if (enabled) {
            Log.d("$TAG-$tag", message)
        }
    }
    
    /**
     * Logs error message
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (enabled) {
            if (throwable != null) {
                Log.e("$TAG-$tag", message, throwable)
            } else {
                Log.e("$TAG-$tag", message)
            }
        }
    }
    
    /**
     * Logs warning message
     */
    fun w(tag: String, message: String) {
        if (enabled) {
            Log.w("$TAG-$tag", message)
        }
    }
    
    /**
     * Logs info message
     */
    fun i(tag: String, message: String) {
        if (enabled) {
            Log.i("$TAG-$tag", message)
        }
    }
    
    /**
     * Marks a banner ad request
     */
    fun markBannerRequest() {
        bannerRequests.incrementAndGet()
        d("Banner", "Banner request marked. Total requests: ${bannerRequests.get()}")
    }
    
    /**
     * Marks a banner ad impression
     */
    fun markBannerImpression() {
        bannerImpressions.incrementAndGet()
        d("Banner", "Banner impression marked. Total impressions: ${bannerImpressions.get()}")
    }
    
    /**
     * Marks an interstitial ad request
     */
    fun markInterstitialRequest() {
        interstitialRequests.incrementAndGet()
        d("Interstitial", "Interstitial request marked. Total requests: ${interstitialRequests.get()}")
    }
    
    /**
     * Marks an interstitial ad impression
     */
    fun markInterstitialImpression() {
        interstitialImpressions.incrementAndGet()
        d("Interstitial", "Interstitial impression marked. Total impressions: ${interstitialImpressions.get()}")
    }
    
    /**
     * Returns summary of ads performance rates
     */
    fun rateSummary(): String {
        val bannerRate = if (bannerRequests.get() > 0) {
            String.format("%.1f", (bannerImpressions.get() * 100.0 / bannerRequests.get()))
        } else "0.0"
        
        val interstitialRate = if (interstitialRequests.get() > 0) {
            String.format("%.1f", (interstitialImpressions.get() * 100.0 / interstitialRequests.get()))
        } else "0.0"
        
        return "Ads Rates - Banner: ${bannerImpressions.get()}/${bannerRequests.get()} ($bannerRate%), " +
                "Interstitial: ${interstitialImpressions.get()}/${interstitialRequests.get()} ($interstitialRate%)"
    }
    
    /**
     * Logs the current ads performance summary
     */
    fun logRateSummary() {
        i("Performance", rateSummary())
    }
    
    /**
     * Resets all counters (useful for testing)
     */
    fun resetCounters() {
        bannerRequests.set(0)
        bannerImpressions.set(0)
        interstitialRequests.set(0)
        interstitialImpressions.set(0)
        d("Performance", "Ads counters reset")
    }
}