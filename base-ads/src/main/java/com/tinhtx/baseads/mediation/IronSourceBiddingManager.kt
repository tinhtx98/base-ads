package com.tinhtx.baseads.mediation

import android.content.Context
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdsLogger
import com.tinhtx.baseads.core.AnalyticsLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages ironSource bidding configuration & analytics (bidding mediation only).
 *
 * With real-time bidding via AdMob mediation, we do NOT manually init ironSource SDK;
 * Google Mobile Ads handles adapter lifecycle. This manager centralizes:
 * - Initialization analytics (so we know ironSource is enabled in config)
 * - Optional verbose logging toggle (for debugging in production safely gated by config)
 * - Bid performance / win-loss analytics hooks (extensible stubs)
 * - Debug info exposed to UI / developer tools
 */
@Singleton
class IronSourceBiddingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adsConfig: AdsConfig,
    private val analyticsLogger: AnalyticsLogger
) {

    companion object {
        private const val TAG = "IronSourceBidding"
        private const val MEDIATION_PARTNER = "ironsource"
    }

    /**
     * Initialize ironSource bidding analytics layer (no network SDK manual init here).
     */
    fun initialize() {
        if (!adsConfig.isIronSourceBiddingEnabled()) {
            AdsLogger.d(TAG, "ironSource bidding disabled in configuration")
            return
        }

        try {
            AdsLogger.i(TAG, "Initializing ironSource bidding analytics")
            analyticsLogger.logEvent(
                "ironsource_bidding_init",
                mapOf(
                    "mediation_type" to "bidding",
                    "partner" to MEDIATION_PARTNER,
                    "logging_enabled" to adsConfig.shouldEnableIronSourceLogging()
                )
            )
            AdsLogger.i(TAG, "ironSource bidding analytics initialized")
        } catch (e: Exception) {
            AdsLogger.e(TAG, "Failed to initialize ironSource bidding analytics", e)
            analyticsLogger.logEvent(
                "ironsource_bidding_init_failed",
                mapOf(
                    "error" to (e.message ?: "unknown"),
                    "partner" to MEDIATION_PARTNER
                )
            )
        }
    }

    /**
     * Generic bidding metrics (extensible). Use for periodic aggregated stats.
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
        analyticsLogger.logEvent("ironsource_bidding_metrics", params)
    }

    /**
     * Hooks for bid outcome tracking (can be called when Google supplies win/loss info in future versions).
     */
    fun logBidWin(adType: String, bidPrice: Double? = null) {
        analyticsLogger.logEvent(
            "ironsource_bid_win",
            mapOf(
                "partner" to MEDIATION_PARTNER,
                "ad_type" to adType,
                "bid_price" to (bidPrice ?: 0.0)
            )
        )
    }

    fun logBidLoss(adType: String, reason: String? = null) {
        val params = mutableMapOf<String, Any>(
            "partner" to MEDIATION_PARTNER,
            "ad_type" to adType
        )
        reason?.let { params["loss_reason"] = it }
        analyticsLogger.logEvent("ironsource_bid_loss", params)
    }

    /**
     * Exposed to UI / debug panels.
     */
    fun getDebugInfo(): Map<String, Any> = mapOf(
        "IronSource Bidding Enabled" to adsConfig.isIronSourceBiddingEnabled(),
        "IronSource Logging" to adsConfig.shouldEnableIronSourceLogging(),
        "Mediation Type" to "Bidding (Real-time)",
        "Partner" to MEDIATION_PARTNER.uppercase(),
        "Analytics Tracking" to adsConfig.shouldTrackMediationAnalytics()
    )

    fun isConfigured(): Boolean = adsConfig.isIronSourceBiddingEnabled()
}
