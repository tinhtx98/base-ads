/*
 * Base Ads Module - Ads Configuration
 * 
 * Data class containing configuration options for ads behavior,
 * including enable/disable flags and blocklist routes.
 */

package com.tinhtx.baseads.core

/**
 * Configuration class for controlling ads behavior throughout the app.
 * 
 * @param enableAds Master toggle for all ads functionality
 * @param enableInterstitial Toggle for interstitial ads specifically
 * @param enableBanner Toggle for banner ads specifically
 * @param interstitialBlocklistRoutes Set of route names where interstitials should never show
 * @param showInterstitialBeforeNavigate If true, shows interstitial before navigation;
 *                                      if false, shows after navigation completion
 * @param ironSourceAppKey ironSource app key for mediation (null to disable ironSource)
 * @param enableIronSourceLogging Enable ironSource SDK logging for debugging
 */
data class AdsConfig(
    val enableAds: Boolean = true,
    val enableInterstitial: Boolean = true,
    val enableBanner: Boolean = true,
    val interstitialBlocklistRoutes: Set<String> = setOf(
        "premium",
        "vip", 
        "subscription",
        "auth",
        "login",
        "register",
        "checkout",
        "payment",
        "purchase",
        "billing"
    ),
    val showInterstitialBeforeNavigate: Boolean = false,
    val ironSourceAppKey: String? = "23b463c45",
    val enableIronSourceLogging: Boolean = true
) {
    
    /**
     * Checks if a route is blocked from showing interstitial ads
     */
    fun isRouteBlocked(route: String?): Boolean {
        return route != null && interstitialBlocklistRoutes.contains(route.lowercase())
    }
    
    /**
     * Checks if ads are completely disabled (master switch or VIP status)
     */
    fun areAdsDisabled(): Boolean = !enableAds
    
    /**
     * Checks if interstitial ads should be shown
     */
    fun shouldShowInterstitial(): Boolean = enableAds && enableInterstitial
    
    /**
     * Checks if banner ads should be shown
     */
    fun shouldShowBanner(): Boolean = enableAds && enableBanner
    
    /**
     * Checks if ironSource mediation is enabled
     */
    fun isIronSourceEnabled(): Boolean {
        return !ironSourceAppKey.isNullOrBlank()
    }
    
    /**
     * Gets the ironSource app key safely
     */
    fun getIronSourceAppKeySafe(): String? {
        return ironSourceAppKey?.takeIf { it.isNotBlank() }
    }
}