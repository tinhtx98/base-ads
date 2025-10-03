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
import androidx.compose.foundation.text.KeyboardActions
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

/**
 * Extension for search actions - analytics only, no ads interruption
 */
@Composable
fun Modifier.searchClickable(
    searchQuery: String,
    analytics: AnalyticsLogger,
    enabled: Boolean = true,
    onClickLabel: String? = "Search",
    onSearch: (String) -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    
    return this.clickable(
        interactionSource = interactionSource,
        indication = ripple(),
        enabled = enabled && searchQuery.isNotBlank(),
        onClickLabel = onClickLabel,
        role = Role.Button
    ) {
        AdsLogger.d("Search", "Search triggered: query='$searchQuery'")
        
        // Log search event with detailed analytics
        analytics.logEvent("search_performed", mapOf(
            "query" to searchQuery,
            "query_length" to searchQuery.length,
            "query_words" to searchQuery.split(" ").size,
            "has_special_chars" to searchQuery.any { !it.isLetterOrDigit() && !it.isWhitespace() },
            "timestamp" to System.currentTimeMillis()
        ))
        
        try {
            onSearch(searchQuery)
            
            AdsLogger.d("Search", "Search completed successfully")
            
            // Log successful search
            analytics.logEvent("search_success", mapOf(
                "query" to searchQuery
            ))
            
        } catch (e: Exception) {
            AdsLogger.e("Search", "Error in search action", e)
            
            analytics.logEvent("search_error", mapOf(
                "query" to searchQuery,
                "error" to e.message
            ))
            
            throw e
        }
    }
}

/**
 * Smart onClick handler for Button components with analytics and optional ads
 */
@Composable
fun createSmartButtonOnClick(
    buttonLabel: String,
    analytics: AnalyticsLogger,
    ads: InterstitialAdManager? = null,
    config: AdsConfig? = null,
    activity: Activity? = null,
    onClick: () -> Unit
): () -> Unit {
    return {
        AdsLogger.d("Button", "Button clicked: $buttonLabel")
        
        // Log button click event
        analytics.logEvent("button_click", mapOf(
            "button_label" to buttonLabel,
            "timestamp" to System.currentTimeMillis()
        ))
        
        try {
            // Execute the click action first
            onClick()
            
            AdsLogger.d("Button", "Button click action completed: $buttonLabel")
            
            // Show interstitial after click if configured
            if (ads != null && config != null && activity != null && !config.showInterstitialBeforeNavigate) {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val adShown = ads.maybeShow(
                            activity = activity,
                            currentRoute = null
                        )
                        
                        AdsLogger.d("Button", "Post-button-click interstitial attempt - shown: $adShown")
                        
                        analytics.logEvent("button_click_ad_attempt", mapOf(
                            "button_label" to buttonLabel,
                            "ad_shown" to adShown
                        ))
                        
                    } catch (e: Exception) {
                        AdsLogger.e("Button", "Error showing post-button-click interstitial", e)
                        
                        analytics.logEvent("button_click_ad_error", mapOf(
                            "button_label" to buttonLabel,
                            "error" to e.message
                        ))
                    }
                }
            }
            
        } catch (e: Exception) {
            AdsLogger.e("Button", "Error in button click action: $buttonLabel", e)
            
            analytics.logEvent("button_click_error", mapOf(
                "button_label" to buttonLabel,
                "error" to e.message
            ))
            
            throw e
        }
    }
}

/**
 * Simple analytics-only onClick handler for Button components
 */
@Composable
fun createAnalyticsButtonOnClick(
    buttonLabel: String,
    analytics: AnalyticsLogger,
    onClick: () -> Unit
): () -> Unit {
    return {
        AdsLogger.d("Button", "Button clicked (analytics only): $buttonLabel")
        
        // Log button click event
        analytics.logEvent("button_click", mapOf(
            "button_label" to buttonLabel,
            "analytics_only" to true,
            "timestamp" to System.currentTimeMillis()
        ))
        
        try {
            onClick()
            
            analytics.logEvent("button_click_success", mapOf(
                "button_label" to buttonLabel
            ))
            
        } catch (e: Exception) {
            AdsLogger.e("Button", "Error in analytics button click action: $buttonLabel", e)
            
            analytics.logEvent("button_click_error", mapOf(
                "button_label" to buttonLabel,
                "error" to e.message
            ))
            
            throw e
        }
    }
}

/**
 * Creates KeyboardActions with smart search analytics tracking
 */
@Composable
fun createSmartSearchKeyboardActions(
    searchQuery: String,
    analytics: AnalyticsLogger,
    onSearch: (String) -> Unit
): KeyboardActions {
    return KeyboardActions(
        onSearch = {
            AdsLogger.d("Keyboard", "Search triggered via keyboard: query='$searchQuery'")
            
            if (searchQuery.isNotBlank()) {
                // Log search event with detailed analytics
                analytics.logEvent("keyboard_search_performed", mapOf(
                    "query" to searchQuery,
                    "query_length" to searchQuery.length,
                    "query_words" to searchQuery.split(" ").size,
                    "has_special_chars" to searchQuery.any { !it.isLetterOrDigit() && !it.isWhitespace() },
                    "input_method" to "keyboard",
                    "timestamp" to System.currentTimeMillis()
                ))
                
                try {
                    onSearch(searchQuery)
                    
                    AdsLogger.d("Keyboard", "Keyboard search completed successfully")
                    
                    // Log successful search
                    analytics.logEvent("keyboard_search_success", mapOf(
                        "query" to searchQuery,
                        "input_method" to "keyboard"
                    ))
                    
                } catch (e: Exception) {
                    AdsLogger.e("Keyboard", "Error in keyboard search action", e)
                    
                    analytics.logEvent("keyboard_search_error", mapOf(
                        "query" to searchQuery,
                        "error" to e.message,
                        "input_method" to "keyboard"
                    ))
                    
                    throw e
                }
            } else {
                // Log empty search attempt
                analytics.logEvent("keyboard_search_empty", mapOf(
                    "input_method" to "keyboard",
                    "timestamp" to System.currentTimeMillis()
                ))
            }
        }
    )
}