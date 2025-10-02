/*
 * Base Ads Module - Clickable Extensions
 * 
 * Extensions for Modifier.clickable to integrate smart ad showing with user interactions.
 * Provides analytics tracking and optional ad display after clicks.
 */

package com.tinhtx.baseads.ext

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdsLogger
import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.interstitial.InterstitialAdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Modifier extension that adds clickable behavior with smart ads integration.
 * Logs click events and optionally shows interstitial ads after click action.
 * 
 * @param label Analytics label for the click event
 * @param ads Interstitial ad manager
 * @param analytics Analytics logger
 * @param config Ads configuration
 * @param activity Current activity (required if ads might be shown)
 * @param enabled Whether the clickable is enabled
 * @param onClickLabel Semantic label for accessibility
 * @param role Semantic role for accessibility
 * @param rippleColor Color of the ripple effect
 * @param bounded Whether ripple effect is bounded
 * @param onClick Action to perform when clicked
 */
@Composable
fun Modifier.smartClickable(
    label: String,
    ads: InterstitialAdManager,
    analytics: AnalyticsLogger,
    config: AdsConfig,
    activity: Activity?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    rippleColor: Color = Color.Unspecified,
    bounded: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = ripple(
        color = rippleColor,
        bounded = bounded
    )
    
    return this.clickable(
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role
    ) {
        AdsLogger.d("Click", "Smart click triggered: $label")
        
        // Log click event
        analytics.logEvent("click", mapOf(
            "label" to label,
            "enabled" to enabled,
            "timestamp" to System.currentTimeMillis()
        ))
        
        try {
            // Execute the click action first
            onClick()
            
            AdsLogger.d("Click", "Click action completed: $label")
            
            // Show interstitial after click if configured and activity is available
            if (!config.showInterstitialBeforeNavigate && activity != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val adShown = ads.maybeShow(
                            activity = activity,
                            currentRoute = null // No specific route for generic clicks
                        )
                        
                        AdsLogger.d("Click", "Post-click interstitial attempt - shown: $adShown")
                        
                        analytics.logEvent("click_ad_attempt", mapOf(
                            "label" to label,
                            "ad_shown" to adShown
                        ))
                        
                    } catch (e: Exception) {
                        AdsLogger.e("Click", "Error showing post-click interstitial", e)
                        
                        analytics.logEvent("click_ad_error", mapOf(
                            "label" to label,
                            "error" to e.message
                        ))
                    }
                }
            }
            
        } catch (e: Exception) {
            AdsLogger.e("Click", "Error in click action: $label", e)
            
            analytics.logEvent("click_error", mapOf(
                "label" to label,
                "error" to e.message
            ))
            
            throw e
        }
    }
}

/**
 * Simplified version of smartClickable without ripple customization
 */
@Composable
fun Modifier.smartClickableSimple(
    label: String,
    ads: InterstitialAdManager,
    analytics: AnalyticsLogger,
    config: AdsConfig,
    activity: Activity?,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    return smartClickable(
        label = label,
        ads = ads,
        analytics = analytics,
        config = config,
        activity = activity,
        enabled = enabled,
        onClick = onClick
    )
}

/**
 * Version of smartClickable that doesn't show ads (only analytics)
 */
@Composable
fun Modifier.clickableWithAnalytics(
    label: String,
    analytics: AnalyticsLogger,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    
    return this.clickable(
        interactionSource = interactionSource,
        indication = ripple(),
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role
    ) {
        AdsLogger.d("Click", "Analytics click triggered: $label")
        
        // Log click event
        analytics.logEvent("click", mapOf(
            "label" to label,
            "enabled" to enabled,
            "timestamp" to System.currentTimeMillis()
        ))
        
        try {
            onClick()
        } catch (e: Exception) {
            AdsLogger.e("Click", "Error in analytics click action: $label", e)
            
            analytics.logEvent("click_error", mapOf(
                "label" to label,
                "error" to e.message
            ))
            
            throw e
        }
    }
}

/**
 * Extension for tracking button clicks with specific button types
 */
@Composable
fun Modifier.smartButtonClick(
    buttonType: String,
    buttonId: String,
    ads: InterstitialAdManager,
    analytics: AnalyticsLogger,
    config: AdsConfig,
    activity: Activity?,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    return smartClickable(
        label = "${buttonType}_${buttonId}",
        ads = ads,
        analytics = analytics,
        config = config,
        activity = activity,
        enabled = enabled,
        onClick = {
            // Log specific button event
            analytics.logEvent("button_click", mapOf(
                "button_type" to buttonType,
                "button_id" to buttonId,
                "enabled" to enabled
            ))
            
            onClick()
        }
    )
}