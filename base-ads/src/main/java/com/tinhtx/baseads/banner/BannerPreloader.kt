/*
 * Base Ads Module - Banner Preloader
 * 
 * Preloads banner ads to reduce display delay on first screen load.
 * Should be called during app initialization or activity creation.
 */

package com.tinhtx.baseads.banner

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.tinhtx.baseads.core.AdUnitsProvider
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdsInitializer
import com.tinhtx.baseads.core.AdsLogger
import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.core.AdRevenueReporter
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.AdValue
import com.tinhtx.baseads.core.VipGate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preloads banner ads to improve display performance.
 * 
 * Usage:
 * ```kotlin
 * // In MainActivity.onCreate()
 * bannerPreloader.preloadBanner()
 * ```
 */
@Singleton
class BannerPreloader @Inject constructor(
    private val adUnitsProvider: AdUnitsProvider,
    private val adsConfig: AdsConfig,
    private val vipGate: VipGate,
    private val analyticsLogger: AnalyticsLogger,
    private val adsInitializer: AdsInitializer,
    private val adRevenueReporter: AdRevenueReporter
) {
    
    private var preloadedAdView: AdView? = null
    private var isPreloading = false
    private var isPreloaded = false
    
    /**
     * Preloads a banner ad to improve display performance
     */
    fun preloadBanner(context: Context) {
        if (isPreloading || isPreloaded || vipGate.isVip() || !adsConfig.shouldShowBanner()) {
            AdsLogger.d("BannerPreloader", "Skipping preload - already loading/loaded or disabled")
            return
        }

        isPreloading = true
        AdsLogger.d("BannerPreloader", "Starting banner preload")

        // Wait for ads initialization before preloading
        adsInitializer.onInitialized {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    preloadedAdView = AdView(context).apply {
                        // Set adaptive banner size
                        val displayMetrics = context.resources.displayMetrics
                        val adWidthDp =
                            (displayMetrics.widthPixels / displayMetrics.density).toInt()
                        val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            context,
                            adWidthDp
                        )

                        setAdSize(adaptiveSize)
                        adUnitId = adUnitsProvider.bannerAdUnitId

                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                AdsLogger.d("BannerPreloader", "Banner preloaded successfully")
                                isPreloaded = true
                                isPreloading = false

                                analyticsLogger.logEvent(
                                    "banner_preloaded", mapOf(
                                        "ad_unit_id" to adUnitsProvider.bannerAdUnitId
                                    )
                                )
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                AdsLogger.e(
                                    "BannerPreloader",
                                    "Banner preload failed: ${loadAdError.code} - ${loadAdError.message}"
                                )
                                isPreloading = false

                                analyticsLogger.logEvent(
                                    "banner_preload_failed", mapOf(
                                        "error_code" to loadAdError.code,
                                        "error_message" to loadAdError.message
                                    )
                                )
                            }
                        }

                        // Attach ILRD (paid) listener
                        onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                            adRevenueReporter.reportInterstitialRevenue( // reuse generic method name
                                adUnitsProvider.bannerAdUnitId,
                                adValue.valueMicros,
                                adValue.currencyCode,
                                adValue.precisionType,
                                route = null
                            )
                        }

                        // Load the ad
                        val adRequest = AdRequest.Builder().build()
                        loadAd(adRequest)
                    }

                } catch (e: Exception) {
                    AdsLogger.e("BannerPreloader", "Failed to create preload AdView", e)
                    isPreloading = false
                }
            }
        }
    }
    
    /**
     * Gets the preloaded banner AdView if available
     */
    fun getPreloadedAdView(): AdView? {
        return if (isPreloaded) {
            val adView = preloadedAdView
            preloadedAdView = null // Clear reference to prevent reuse
            isPreloaded = false
            adView
        } else {
            null
        }
    }
    
    /**
     * Clears the preloaded ad if not used
     */
    fun clearPreloadedAd() {
        preloadedAdView?.destroy()
        preloadedAdView = null
        isPreloaded = false
        isPreloading = false
        AdsLogger.d("BannerPreloader", "Preloaded ad cleared")
    }
    
    /**
     * Checks if ads are initialized (simplified check)
     */
    private fun isAdsInitialized(): Boolean {
        return adsInitializer.isInitialized()
    }
}