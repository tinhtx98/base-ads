/*
 * Base Ads Module - Navigation Controller Extensions
 * 
 * Extensions for NavController to integrate smart ad showing with navigation.
 * Includes composable helpers for screen tracking.
 */

package com.tinhtx.baseads.ext

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdsLogger
import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.interstitial.InterstitialAdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Navigates to a route with smart interstitial ad integration.
 * Can show ads before or after navigation based on configuration.
 * 
 * @param route Destination route
 * @param ads Interstitial ad manager
 * @param analytics Analytics logger
 * @param config Ads configuration
 * @param activity Current activity for showing ads
 * @param launchSingleTop Whether to launch as single top
 * @param restoreState Whether to restore state
 * @param popUpTo Route to pop up to (optional)
 * @param inclusive Whether pop up should be inclusive
 */
suspend fun NavController.navigateSmart(
    route: String,
    ads: InterstitialAdManager,
    analytics: AnalyticsLogger,
    config: AdsConfig,
    activity: Activity,
    launchSingleTop: Boolean = false,
    restoreState: Boolean = false,
    popUpTo: String? = null,
    inclusive: Boolean = false
) {
    AdsLogger.d("Navigation", "Smart navigation to: $route")
    
    // Log navigation attempt
    analytics.logEvent("nav_click", mapOf(
        "route" to route,
        "from_route" to (currentDestination?.route ?: "unknown"),
        "show_before_navigate" to config.showInterstitialBeforeNavigate
    ))
    
    try {
        if (config.showInterstitialBeforeNavigate) {
            // Show interstitial before navigation
            AdsLogger.d("Navigation", "Attempting to show interstitial before navigation")
            
            val adShown = ads.maybeShow(
                activity = activity,
                currentRoute = route
            )
            
            AdsLogger.d("Navigation", "Interstitial before navigation - shown: $adShown")
            
            // Navigate after ad (whether shown or not)
            performNavigation(route, launchSingleTop, restoreState, popUpTo, inclusive)
            
        } else {
            // Navigate first, then try to show interstitial
            performNavigation(route, launchSingleTop, restoreState, popUpTo, inclusive)
            
            AdsLogger.d("Navigation", "Navigation completed, attempting to show interstitial")
            
            // Show interstitial after navigation (non-blocking)
            CoroutineScope(Dispatchers.Main).launch {
                val adShown = ads.maybeShow(
                    activity = activity,
                    currentRoute = route
                )
                
                AdsLogger.d("Navigation", "Interstitial after navigation - shown: $adShown")
            }
        }
        
        // Log successful navigation
        analytics.logEvent("nav_success", mapOf(
            "route" to route,
            "navigation_mode" to if (config.showInterstitialBeforeNavigate) "before_ad" else "after_ad"
        ))
        
    } catch (e: Exception) {
        AdsLogger.e("Navigation", "Navigation failed to $route", e)
        
        analytics.logEvent("nav_error", mapOf(
            "route" to route,
            "error" to e.message
        ))
        
        throw e
    }
}

/**
 * Performs the actual navigation with the specified parameters
 */
private fun NavController.performNavigation(
    route: String,
    launchSingleTop: Boolean,
    restoreState: Boolean,
    popUpTo: String?,
    inclusive: Boolean
) {
    navigate(route) {
        this.launchSingleTop = launchSingleTop
        this.restoreState = restoreState
        
        popUpTo?.let { popRoute ->
            popUpTo(popRoute) {
                this.inclusive = inclusive
            }
        }
    }
}

/**
 * Convenience method for simple navigation with smart ads
 */
suspend fun NavController.navigateSmartSimple(
    route: String,
    ads: InterstitialAdManager,
    analytics: AnalyticsLogger,
    config: AdsConfig,
    activity: Activity
) {
    navigateSmart(
        route = route,
        ads = ads,
        analytics = analytics,
        config = config,
        activity = activity
    )
}

/**
 * Composable that marks screen as opened and sets current screen for analytics.
 * Should be called in each screen's Composable.
 * 
 * @param ads Interstitial ad manager
 * @param analytics Analytics logger
 * @param screenName Name of the current screen
 */
@Composable
fun MarkScreenOpened(
    ads: InterstitialAdManager,
    analytics: AnalyticsLogger,
    screenName: String
) {
    DisposableEffect(screenName) {
        // Mark screen opened for ad timing
        ads.markScreenOpened()
        
        // Set current screen for analytics
        analytics.setCurrentScreen(screenName)
        
        AdsLogger.d("Navigation", "Screen marked as opened: $screenName")
        
        onDispose {
            AdsLogger.d("Navigation", "Screen disposed: $screenName")
        }
    }
}

/**
 * Composable that tracks screen views with additional parameters
 * 
 * @param ads Interstitial ad manager
 * @param analytics Analytics logger
 * @param screenName Name of the current screen
 * @param extraParams Additional parameters to log with screen view
 */
@Composable
fun MarkScreenOpenedWithParams(
    ads: InterstitialAdManager,
    analytics: AnalyticsLogger,
    screenName: String,
    extraParams: Map<String, Any> = emptyMap()
) {
    DisposableEffect(screenName, extraParams) {
        // Mark screen opened for ad timing
        ads.markScreenOpened()
        
        // Set current screen for analytics
        analytics.setCurrentScreen(screenName)
        
        // Log screen view with extra parameters
        if (extraParams.isNotEmpty()) {
            analytics.logEvent("screen_view_detailed", mapOf(
                "screen_name" to screenName
            ) + extraParams)
        }
        
        AdsLogger.d("Navigation", "Screen with params marked as opened: $screenName, params: $extraParams")
        
        onDispose {
            AdsLogger.d("Navigation", "Screen with params disposed: $screenName")
        }
    }
}

/**
 * Extension to get current route name safely
 */
val NavController.currentRouteName: String?
    get() = currentDestination?.route

/**
 * Extension to check if currently on a specific route
 */
fun NavController.isOnRoute(route: String): Boolean {
    return currentDestination?.route == route
}