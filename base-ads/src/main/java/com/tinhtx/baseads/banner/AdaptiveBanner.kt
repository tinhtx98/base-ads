/*
 * Base Ads Module - Adaptive Banner
 * 
 * Jetpack Compose component for displaying AdMob Anchored Adaptive Banner ads.
 * Automatically calculates optimal banner size based on screen width.
 */

package com.tinhtx.baseads.banner

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.tinhtx.baseads.core.AdUnitsProvider
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdsLogger
import com.tinhtx.baseads.core.AdStatus
import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.core.VipGate

/**
 * Composable that displays an AdMob Anchored Adaptive Banner.
 * Automatically handles size calculation and ad loading.
 * 
 * @param adUnitsProvider Provider for ad unit IDs
 * @param adsConfig Configuration for ads behavior
 * @param vipGate VIP status checker
 * @param analyticsLogger Analytics event logger
 * @param modifier Compose modifier for styling
 */
@Composable
fun AdaptiveBanner(
    adUnitsProvider: AdUnitsProvider,
    adsConfig: AdsConfig,
    vipGate: VipGate,
    analyticsLogger: AnalyticsLogger,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val bannerId = "banner_bottom"  // ID for bottom banner
    
    // Don't show banner if VIP or disabled
    if (vipGate.isVip() || !adsConfig.shouldShowBanner()) {
        DisposableEffect(Unit) {
            AdsLogger.registerAd(bannerId, "banner")
            AdsLogger.updateAdStatus(bannerId, AdStatus.NOT_AVAILABLE,
                if (vipGate.isVip()) "VIP user" else "Banner disabled")
            AdsLogger.d("Banner", "Banner disabled - VIP: ${vipGate.isVip()}, Config: ${adsConfig.shouldShowBanner()}")
            
            onDispose {
                AdsLogger.unregisterAd(bannerId)
            }
        }
        return
    }
    
    var bannerHeight by remember { mutableStateOf(60.dp) } // Start with estimated height
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight),
        factory = { ctx ->
            AdsLogger.registerAd(bannerId, "banner")
            AdsLogger.updateAdStatus(bannerId, AdStatus.INITIALIZING)
            AdsLogger.d("Banner", "Creating AdView")
            AdsLogger.markBannerRequest()
            
            AdView(ctx).apply {
                // Get actual available width from the context
                // Use display metrics for full width banner
                val displayMetrics = ctx.resources.displayMetrics
                val displayDensity = displayMetrics.density
                val adWidthPixels = displayMetrics.widthPixels
                
                // Convert to dp for AdSize calculation
                var adWidthDp = (adWidthPixels / displayDensity).toInt()
                
                AdsLogger.d("Banner", "Screen width: ${adWidthDp}dp, pixels: $adWidthPixels")
                
                // Get adaptive ad size for current orientation
                val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    ctx, 
                    adWidthDp
                )
                
                AdsLogger.d(
                    "Banner", 
                    "AdSize requested: ${adWidthDp}dp, " +
                    "AdSize returned: ${adaptiveSize.width}x${adaptiveSize.height}dp"
                )
                
                // Set the ad size and unit ID
                setAdSize(adaptiveSize)
                adUnitId = adUnitsProvider.bannerAdUnitId
                
                // Calculate actual banner height in dp and update state
                val heightInPixels = adaptiveSize.getHeightInPixels(ctx)
                val heightInDp = (heightInPixels / displayDensity)
                bannerHeight = heightInDp.dp
                
                AdsLogger.d(
                    "Banner", 
                    "Banner height: ${heightInDp}dp (${heightInPixels}px)"
                )
                
                // Set ad listener
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        AdsLogger.updateAdStatus(bannerId, AdStatus.READY)
                        AdsLogger.d("Banner", "Banner ad loaded successfully")
                        AdsLogger.markBannerImpression()
                        
                        analyticsLogger.logEvent("ad_banner_loaded", mapOf(
                            "ad_unit_id" to adUnitsProvider.bannerAdUnitId,
                            "ad_width" to adaptiveSize.width,
                            "ad_height" to adaptiveSize.height
                        ))
                    }
                    
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        val errorMsg = "Code: ${loadAdError.code} - ${loadAdError.message}"
                        AdsLogger.updateAdStatus(bannerId, AdStatus.FAILED, errorMsg)
                        AdsLogger.e(
                            "Banner",
                            "Banner ad failed to load. $errorMsg, Domain: ${loadAdError.domain}"
                        )
                        
                        analyticsLogger.logEvent("ad_banner_load_failed", mapOf(
                            "error_code" to loadAdError.code,
                            "error_message" to loadAdError.message,
                            "error_domain" to loadAdError.domain,
                            "ad_unit_id" to adUnitsProvider.bannerAdUnitId
                        ))
                    }
                    
                    override fun onAdImpression() {
                        AdsLogger.updateAdStatus(bannerId, AdStatus.SHOWING)
                        AdsLogger.d("Banner", "Banner ad impression")
                        
                        analyticsLogger.logEvent("ad_banner_impression", mapOf(
                            "ad_unit_id" to adUnitsProvider.bannerAdUnitId
                        ))
                    }
                    
                    override fun onAdClicked() {
                        AdsLogger.d("Banner", "Banner ad clicked")
                        
                        analyticsLogger.logEvent("ad_banner_clicked", mapOf(
                            "ad_unit_id" to adUnitsProvider.bannerAdUnitId
                        ))
                    }
                    
                    override fun onAdOpened() {
                        AdsLogger.d("Banner", "Banner ad opened")
                    }
                    
                    override fun onAdClosed() {
                        AdsLogger.d("Banner", "Banner ad closed")
                    }
                }
                
                // Load the ad
                val adRequest = AdRequest.Builder().build()
                loadAd(adRequest)
                
                AdsLogger.d("Banner", "Banner ad request sent")
            }
        },
        update = { adView ->
            // Handle updates if needed (e.g., configuration changes)
            AdsLogger.d("Banner", "AdView update called")
        }
    )
    
    // Clean up when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            AdsLogger.d("Banner", "Banner composable disposed")
            AdsLogger.unregisterAd(bannerId)
        }
    }
}

/**
 * Preview-friendly version of AdaptiveBanner that shows a placeholder
 */
@Composable
fun AdaptiveBannerPreview(
    modifier: Modifier = Modifier
) {
    // This would show a placeholder in preview mode
    // In actual implementation, you might want to show a colored rectangle
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        // Preview placeholder
    }
}