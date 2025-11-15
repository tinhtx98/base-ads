package com.tinhtx.baseads.core

import com.tinhtx.baseads.core.AdsLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight revenue reporter for ILRD (Impression Level Revenue Data).
 * Currently logs via AnalyticsLogger; can be extended to batch & POST to server.
 */
@Singleton
class AdRevenueReporter @Inject constructor(
    private val analyticsLogger: AnalyticsLogger,
    private val adsConfig: AdsConfig
) {
    /**
     * Reports interstitial revenue.
     * @param adUnitId AdMob ad unit id
     * @param valueMicros Revenue micros (1_000_000 micros = 1 unit currency)
     * @param currency ISO 4217 currency code
     * @param precision Google Mobile Ads precision enum value
     * @param adapterClassName Mediation adapter class name (for attribution)
     * @param route Optional route/screen name at show time
     */
    fun reportInterstitialRevenue(
        adUnitId: String,
        valueMicros: Long,
        currency: String?,
        precision: Int,
        adapterClassName: String?,
        route: String?
    ) {
        if (!adsConfig.shouldTrackMediationAnalytics()) return

        val revenue = valueMicros / 1_000_000.0
        val adapterName = extractAdapterName(adapterClassName)
        
        AdsLogger.d(
            "Revenue",
            "ILRD interstitial revenue=$revenue $currency (micros=$valueMicros, precision=$precision, adapter=$adapterName, route=$route)"
        )

        analyticsLogger.logEvent(
            "ad_interstitial_paid",
            mapOf(
                "ad_unit_id" to adUnitId,
                "revenue_micros" to valueMicros,
                "revenue" to revenue,
                "currency" to (currency ?: "UNKNOWN"),
                "precision" to precision,
                "adapter_class_name" to (adapterClassName ?: "unknown"),
                "adapter_name" to adapterName,
                "route" to (route ?: "unknown")
            )
        )
    }
    
    /**
     * Reports banner revenue.
     * @param adUnitId AdMob ad unit id
     * @param valueMicros Revenue micros (1_000_000 micros = 1 unit currency)
     * @param currency ISO 4217 currency code
     * @param precision Google Mobile Ads precision enum value
     * @param adapterClassName Mediation adapter class name (for attribution)
     * @param route Optional route/screen name at show time
     */
    fun reportBannerRevenue(
        adUnitId: String,
        valueMicros: Long,
        currency: String?,
        precision: Int,
        adapterClassName: String?,
        route: String?
    ) {
        if (!adsConfig.shouldTrackMediationAnalytics()) return

        val revenue = valueMicros / 1_000_000.0
        val adapterName = extractAdapterName(adapterClassName)
        
        AdsLogger.d(
            "Revenue",
            "ILRD banner revenue=$revenue $currency (micros=$valueMicros, precision=$precision, adapter=$adapterName, route=$route)"
        )

        analyticsLogger.logEvent(
            "ad_banner_paid",
            mapOf(
                "ad_unit_id" to adUnitId,
                "revenue_micros" to valueMicros,
                "revenue" to revenue,
                "currency" to (currency ?: "UNKNOWN"),
                "precision" to precision,
                "adapter_class_name" to (adapterClassName ?: "unknown"),
                "adapter_name" to adapterName,
                "route" to (route ?: "unknown")
            )
        )
    }
    
    /**
     * Extract friendly adapter name from class name.
     * Examples:
     * - com.google.ads.mediation.ironsource.IronSourceMediationAdapter -> ironsource
     * - com.google.ads.mediation.facebook.FacebookMediationAdapter -> meta
     * - com.google.ads.mediation.vungle.VungleMediationAdapter -> vungle
     */
    private fun extractAdapterName(adapterClassName: String?): String {
        if (adapterClassName == null) return "admob"
        
        return when {
            adapterClassName.contains("ironsource", ignoreCase = true) -> "ironsource"
            adapterClassName.contains("facebook", ignoreCase = true) -> "meta"
            adapterClassName.contains("vungle", ignoreCase = true) -> "vungle"
            adapterClassName.contains("inmobi", ignoreCase = true) -> "inmobi"
            else -> adapterClassName.substringAfterLast('.').lowercase()
        }
    }
}
