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
     * @param route Optional route/screen name at show time
     */
    fun reportInterstitialRevenue(
        adUnitId: String,
        valueMicros: Long,
        currency: String?,
        precision: Int,
        route: String?
    ) {
        if (!adsConfig.shouldTrackMediationAnalytics()) return

        val revenue = valueMicros / 1_000_000.0
        AdsLogger.d(
            "Revenue",
            "ILRD interstitial revenue=$revenue $currency (micros=$valueMicros, precision=$precision, route=$route)"
        )

        analyticsLogger.logEvent(
            "ad_interstitial_paid",
            mapOf(
                "ad_unit_id" to adUnitId,
                "revenue_micros" to valueMicros,
                "revenue" to revenue,
                "currency" to (currency ?: "UNKNOWN"),
                "precision" to precision,
                "route" to (route ?: "unknown")
            )
        )
    }
}
