/*
 * Base Ads - Banner Ad Item Component
 * 
 * Composable wrapper for displaying banner ads as items within lists, 
 * columns, or any scrollable content. Provides consistent spacing and 
 * styling for inline banner ads.
 * 
 * Usage:
 * ```kotlin
 * LazyColumn {
 *     items(dataList) { item ->
 *         ItemContent(item)
 *     }
 *     
 *     // Insert banner after every 5 items
 *     item {
 *         BannerAdItem(
 *             adUnitsProvider = adUnitsProvider,
 *             adsConfig = adsConfig,
 *             vipGate = vipGate,
 *             analyticsLogger = analyticsLogger
 *         )
 *     }
 * }
 * ```
 */

package com.tinhtx.baseads.banner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.tinhtx.baseads.core.AdUnitsProvider
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdsLogger
import com.tinhtx.baseads.core.AdStatus
import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.core.VipGate

/**
 * Banner ad component optimized for use as an item in LazyColumn/LazyRow.
 * Uses stable keys to prevent flickering and unnecessary recomposition.
 * 
 * Features:
 * - Stable AdView instance across recompositions
 * - Prevents flickering when scrolling
 * - Automatically handles spacing with padding
 * - Optional background color
 * - VIP user gating
 * - Analytics integration
 * - Adaptive sizing
 * 
 * IMPORTANT: When using in LazyColumn, provide a stable key:
 * ```
 * LazyColumn {
 *     item(key = "banner_ad_1") {
 *         BannerAdItem(...)
 *     }
 * }
 * ```
 * 
 * @param adUnitsProvider Provider for ad unit IDs
 * @param adsConfig Ads configuration settings
 * @param vipGate VIP status checker
 * @param analyticsLogger Analytics event logger
 * @param modifier Optional modifier for the container
 * @param topPadding Top padding around the banner (default 8.dp)
 * @param bottomPadding Bottom padding around the banner (default 8.dp)
 * @param showBackground Whether to show a subtle background (default false)
 * @param adId Unique identifier for this banner instance (used for stable keys)
 */
@Composable
fun BannerAdItem(
    adUnitsProvider: AdUnitsProvider,
    adsConfig: AdsConfig,
    vipGate: VipGate,
    analyticsLogger: AnalyticsLogger,
    adRevenueReporter: com.tinhtx.baseads.core.AdRevenueReporter,
    modifier: Modifier = Modifier,
    topPadding: androidx.compose.ui.unit.Dp = 8.dp,
    bottomPadding: androidx.compose.ui.unit.Dp = 8.dp,
    showBackground: Boolean = false,
    adId: String = "banner_ad",
    trackRefresh: Boolean = true
) {
    val context = LocalContext.current
    
    // Don't show banner if VIP or disabled
    if (vipGate.isVip() || !adsConfig.shouldShowBanner()) {
        // Register as not available
        DisposableEffect(adId) {
            AdsLogger.registerAd(adId, "banner")
            AdsLogger.updateAdStatus(adId, AdStatus.NOT_AVAILABLE, 
                if (vipGate.isVip()) "VIP user" else "Banner disabled")
            
            onDispose {
                AdsLogger.unregisterAd(adId)
            }
        }
        return
    }
    
    val containerModifier = if (showBackground) {
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(top = topPadding, bottom = bottomPadding)
    } else {
        modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = bottomPadding)
    }
    
    var bannerHeight by remember(adId) { mutableStateOf(60.dp) }
    var refreshCount by remember(adId) { mutableStateOf(0) }
    
    Box(modifier = containerModifier) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight),
            factory = { ctx ->
                AdsLogger.registerAd(adId, "banner")
                AdsLogger.updateAdStatus(adId, AdStatus.INITIALIZING)
                AdsLogger.d("BannerAdItem", "Creating AdView for key: $adId")
                
                AdView(ctx).apply {
                    val displayMetrics = ctx.resources.displayMetrics
                    val displayDensity = displayMetrics.density
                    val adWidthPixels = displayMetrics.widthPixels
                    val adWidthDp = (adWidthPixels / displayDensity).toInt()
                    
                    val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        ctx, 
                        adWidthDp
                    )
                    
                    setAdSize(adaptiveSize)
                    adUnitId = adUnitsProvider.bannerAdUnitId
                    
                    // Calculate banner height
                    val heightInPixels = adaptiveSize.getHeightInPixels(ctx)
                    val heightInDp = (heightInPixels / displayDensity)
                    bannerHeight = heightInDp.dp
                    
                    AdsLogger.d(
                        "BannerAdItem", 
                        "[$adId] Size: ${adaptiveSize.width}x${adaptiveSize.height}dp"
                    )
                    
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            refreshCount++
                            val isInitialLoad = refreshCount == 1
                            val isRefresh = refreshCount > 1
                            
                            AdsLogger.updateAdStatus(adId, AdStatus.READY)
                            AdsLogger.d("BannerAdItem", "[$adId] Ad loaded (refresh #$refreshCount)")
                            
                            val eventParams = mutableMapOf(
                                "ad_unit_id" to adUnitsProvider.bannerAdUnitId,
                                "ad_id" to adId,
                                "ad_width" to adaptiveSize.width.toString(),
                                "ad_height" to adaptiveSize.height.toString()
                            )
                            
                            // Add refresh tracking if enabled
                            if (trackRefresh && adsConfig.shouldTrackBannerRefresh()) {
                                eventParams.putAll(mapOf(
                                    "refresh_count" to refreshCount.toString(),
                                    "refresh_type" to "admob_native",
                                    "is_initial_load" to isInitialLoad.toString(),
                                    "is_auto_refresh" to isRefresh.toString()
                                ))
                            }
                            
                            analyticsLogger.logEvent("ad_banner_item_loaded", eventParams)
                        }
                        
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            val errorMsg = "Code: ${loadAdError.code} - ${loadAdError.message}"
                            AdsLogger.updateAdStatus(adId, AdStatus.FAILED, errorMsg)
                            AdsLogger.e(
                                "BannerAdItem",
                                "[$adId] Ad failed (refresh #$refreshCount): $errorMsg"
                            )
                            
                            val eventParams = mutableMapOf(
                                "error_code" to loadAdError.code.toString(),
                                "error_message" to loadAdError.message,
                                "ad_unit_id" to adUnitsProvider.bannerAdUnitId,
                                "ad_id" to adId
                            )
                            
                            // Add refresh tracking if enabled
                            if (trackRefresh && adsConfig.shouldTrackBannerRefresh()) {
                                eventParams.putAll(mapOf(
                                    "refresh_count" to refreshCount.toString(),
                                    "refresh_type" to "admob_native",
                                    "is_auto_refresh" to (refreshCount > 0).toString()
                                ))
                            }
                            
                            analyticsLogger.logEvent("ad_banner_item_load_failed", eventParams)
                        }
                        
                        override fun onAdImpression() {
                            AdsLogger.updateAdStatus(adId, AdStatus.SHOWING)
                            AdsLogger.d("BannerAdItem", "[$adId] Impression (refresh #$refreshCount)")
                            
                            val eventParams = mutableMapOf(
                                "ad_unit_id" to adUnitsProvider.bannerAdUnitId,
                                "ad_id" to adId
                            )
                            
                            // Add refresh tracking if enabled
                            if (trackRefresh && adsConfig.shouldTrackBannerRefresh()) {
                                eventParams["refresh_count"] = refreshCount.toString()
                            }
                            
                            analyticsLogger.logEvent("ad_banner_item_impression", eventParams)
                        }
                        
                        override fun onAdClicked() {
                            AdsLogger.d("BannerAdItem", "[$adId] Clicked (refresh #$refreshCount)")
                            
                            val eventParams = mutableMapOf(
                                "ad_unit_id" to adUnitsProvider.bannerAdUnitId,
                                "ad_id" to adId
                            )
                            
                            // Add refresh tracking if enabled
                            if (trackRefresh && adsConfig.shouldTrackBannerRefresh()) {
                                eventParams["refresh_count"] = refreshCount.toString()
                            }
                            
                            analyticsLogger.logEvent("ad_banner_item_clicked", eventParams)
                        }
                    }
                    
                    // Attach ILRD listener for banner revenue
                    onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                        adRevenueReporter.reportInterstitialRevenue(
                            adUnitsProvider.bannerAdUnitId,
                            adValue.valueMicros,
                            adValue.currencyCode,
                            adValue.precisionType,
                            route = null
                        )
                    }

                    // Load ad with AdMob native refresh
                    // NOTE: Auto-refresh is configured in AdMob Console, not in code
                    // This ensures AdMob handles refresh timing optimally
                    val adRequest = AdRequest.Builder().build()
                    loadAd(adRequest)
                    
                    AdsLogger.d("BannerAdItem", "[$adId] Ad request sent")
                }
            },
            update = { adView ->
                // Don't reload on update to prevent flickering
                // AdMob native refresh handles automatic refreshing
                AdsLogger.d("BannerAdItem", "[$adId] Update called (no action)")
            }
        )
    }
    
    DisposableEffect(adId) {
        AdsLogger.d("BannerAdItem", "[$adId] Composed")
        
        onDispose {
            AdsLogger.d("BannerAdItem", "[$adId] Disposed (total refreshes: $refreshCount)")
            AdsLogger.unregisterAd(adId)
        }
    }
}

/**
 * Banner ad item with card-like appearance for inline content.
 * Best used when you want the banner to visually match other card items.
 * Optimized for LazyColumn with stable keys.
 * 
 * @param adUnitsProvider Provider for ad unit IDs
 * @param adsConfig Ads configuration settings
 * @param vipGate VIP status checker
 * @param analyticsLogger Analytics event logger
 * @param modifier Optional modifier for the container
 * @param adId Unique identifier for this banner instance (used for stable keys)
 */
@Composable
fun BannerAdCard(
    adUnitsProvider: AdUnitsProvider,
    adsConfig: AdsConfig,
    vipGate: VipGate,
    analyticsLogger: AnalyticsLogger,
    modifier: Modifier = Modifier,
    adId: String = "banner_ad_card"
) {
    val context = LocalContext.current
    
    // Don't show banner if VIP or disabled
    if (vipGate.isVip() || !adsConfig.shouldShowBanner()) {
        // Register as not available
        DisposableEffect(adId) {
            AdsLogger.registerAd(adId, "banner")
            AdsLogger.updateAdStatus(adId, AdStatus.NOT_AVAILABLE,
                if (vipGate.isVip()) "VIP user" else "Banner disabled")
            
            onDispose {
                AdsLogger.unregisterAd(adId)
            }
        }
        return
    }
    
    var bannerHeight by remember(adId) { mutableStateOf(60.dp) }
    
    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight),
            factory = { ctx ->
                AdsLogger.registerAd(adId, "banner")
                AdsLogger.updateAdStatus(adId, AdStatus.INITIALIZING)
                AdsLogger.d("BannerAdCard", "Creating AdView for key: $adId")
                
                AdView(ctx).apply {
                    val displayMetrics = ctx.resources.displayMetrics
                    val displayDensity = displayMetrics.density
                    val adWidthPixels = displayMetrics.widthPixels
                    val adWidthDp = (adWidthPixels / displayDensity).toInt()
                    
                    val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        ctx, 
                        adWidthDp
                    )
                    
                    setAdSize(adaptiveSize)
                    adUnitId = adUnitsProvider.bannerAdUnitId
                    
                    // Calculate banner height
                    val heightInPixels = adaptiveSize.getHeightInPixels(ctx)
                    val heightInDp = (heightInPixels / displayDensity)
                    bannerHeight = heightInDp.dp
                    
                    AdsLogger.d(
                        "BannerAdCard", 
                        "[$adId] Size: ${adaptiveSize.width}x${adaptiveSize.height}dp"
                    )
                    
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            AdsLogger.updateAdStatus(adId, AdStatus.READY)
                            AdsLogger.d("BannerAdCard", "[$adId] Ad loaded")
                            
                            analyticsLogger.logEvent("ad_banner_card_loaded", mapOf(
                                "ad_unit_id" to adUnitsProvider.bannerAdUnitId,
                                "ad_id" to adId,
                                "ad_width" to adaptiveSize.width,
                                "ad_height" to adaptiveSize.height
                            ))
                        }
                        
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            val errorMsg = "Code: ${loadAdError.code} - ${loadAdError.message}"
                            AdsLogger.updateAdStatus(adId, AdStatus.FAILED, errorMsg)
                            AdsLogger.e(
                                "BannerAdCard",
                                "[$adId] Ad failed: $errorMsg"
                            )
                            
                            analyticsLogger.logEvent("ad_banner_card_load_failed", mapOf(
                                "error_code" to loadAdError.code,
                                "error_message" to loadAdError.message,
                                "ad_unit_id" to adUnitsProvider.bannerAdUnitId,
                                "ad_id" to adId
                            ))
                        }
                        
                        override fun onAdImpression() {
                            AdsLogger.updateAdStatus(adId, AdStatus.SHOWING)
                            AdsLogger.d("BannerAdCard", "[$adId] Impression")
                            
                            analyticsLogger.logEvent("ad_banner_card_impression", mapOf(
                                "ad_unit_id" to adUnitsProvider.bannerAdUnitId,
                                "ad_id" to adId
                            ))
                        }
                        
                        override fun onAdClicked() {
                            AdsLogger.d("BannerAdCard", "[$adId] Clicked")
                            
                            analyticsLogger.logEvent("ad_banner_card_clicked", mapOf(
                                "ad_unit_id" to adUnitsProvider.bannerAdUnitId,
                                "ad_id" to adId
                            ))
                        }
                    }
                    
                    // Load ad
                    val adRequest = AdRequest.Builder().build()
                    loadAd(adRequest)
                    
                    AdsLogger.d("BannerAdCard", "[$adId] Ad request sent")
                }
            },
            update = { adView ->
                // Don't reload on update to prevent flickering
                AdsLogger.d("BannerAdCard", "[$adId] Update called (no action)")
            }
        )
    }
    
    DisposableEffect(adId) {
        AdsLogger.d("BannerAdCard", "[$adId] Composed")
        
        onDispose {
            AdsLogger.d("BannerAdCard", "[$adId] Disposed")
            AdsLogger.unregisterAd(adId)
        }
    }
}
