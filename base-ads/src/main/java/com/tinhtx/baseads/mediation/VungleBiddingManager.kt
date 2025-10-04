/*
 * Base Ads Module - Vungle Liftoff Bidding Manager
 * 
 * Manages Vungle Liftoff Monetize bidding analytics and configuration.
 * For bidding mediation, all actual ad serving is handled by Google Mobile Ads SDK.
 */

package com.tinhtx.baseads.mediation

import android.content.Context
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdsLogger
import com.tinhtx.baseads.core.AnalyticsLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Vungle Liftoff Monetize bidding configuration and analytics.
 * 
 * Note: With bidding mediation, this class only handles:
 * - Analytics tracking for Vungle performance
 * - Configuration validation
 * - Debug information
 * 
 * Actual ad serving is automatically handled by Google Mobile Ads SDK bidding.
 */
@Singleton
class VungleBiddingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adsConfig: AdsConfig,
    private val analyticsLogger: AnalyticsLogger
) {
    
    companion object {
        private const val TAG = "VungleBidding"
        private const val MEDIATION_PARTNER = "vungle_liftoff"
    }
    
    /**
     * Initializes Vungle bidding analytics tracking
     */
    fun initialize() {
        if (!adsConfig.isVungleBiddingEnabled()) {
            AdsLogger.d(TAG, "Vungle bidding disabled in configuration")
            return
        }
        
        try {
            AdsLogger.i(TAG, "Initializing Vungle Liftoff bidding analytics")
            
            // Log bidding initialization
            analyticsLogger.logEvent("vungle_bidding_init", mapOf(
                "mediation_type" to "bidding",
                "partner" to MEDIATION_PARTNER,
                "logging_enabled" to adsConfig.shouldEnableVungleLogging()
            ))
            
            AdsLogger.i(TAG, "Vungle Liftoff bidding analytics initialized")
            
        } catch (e: Exception) {
            AdsLogger.e(TAG, "Failed to initialize Vungle bidding analytics", e)
            
            analyticsLogger.logEvent("vungle_bidding_init_failed", mapOf(
                "error" to e.message,
                "partner" to MEDIATION_PARTNER
            ))
        }
    }
    
    /**
     * Logs Vungle bidding performance metrics
     */
    fun logBiddingMetrics(adType: String, revenue: Double? = null, winRate: Double? = null) {
        if (!adsConfig.shouldTrackMediationAnalytics()) return
        
        val params = mutableMapOf<String, Any>(
            "partner" to MEDIATION_PARTNER,
            "ad_type" to adType,
            "mediation_type" to "bidding"
        )
        
        revenue?.let { params["revenue"] = it }
        winRate?.let { params["win_rate"] = it }
        
        analyticsLogger.logEvent("vungle_bidding_metrics", params)
    }
    
    /**
     * Logs when Vungle wins a bid
     */
    fun logBidWin(adType: String, bidPrice: Double? = null) {
        analyticsLogger.logEvent("vungle_bid_win", mapOf(
            "partner" to MEDIATION_PARTNER,
            "ad_type" to adType,
            "bid_price" to (bidPrice ?: 0.0)
        ))
    }
    
    /**
     * Logs when Vungle loses a bid
     */
    fun logBidLoss(adType: String, reason: String? = null) {
        val params = mutableMapOf<String, Any>(
            "partner" to MEDIATION_PARTNER,
            "ad_type" to adType
        )
        
        reason?.let { params["loss_reason"] = it }
        
        analyticsLogger.logEvent("vungle_bid_loss", params)
    }
    
    /**
     * Gets debug information about Vungle bidding configuration
     */
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "Vungle Bidding Enabled" to adsConfig.isVungleBiddingEnabled(),
            "Vungle Logging" to adsConfig.shouldEnableVungleLogging(),
            "Mediation Type" to "Bidding (Real-time)",
            "Partner" to MEDIATION_PARTNER.uppercase(),
            "Analytics Tracking" to adsConfig.shouldTrackMediationAnalytics()
        )
    }
    
    /**
     * Checks if Vungle bidding is properly configured
     */
    fun isConfigured(): Boolean {
        return adsConfig.isVungleBiddingEnabled()
    }
}