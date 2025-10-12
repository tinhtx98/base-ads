/*
 * Base Ads Module - Ads Configuration
 * 
 * Data class containing configuration options for ads behavior,
 * including enable/disable flags and blocklist routes.
 */

package com.tinhtx.baseads.core

/**
 * Configuration class for controlling ads behavior throughout the app.
 * Pure bidding architecture (AdMob primary) with optional secondary bidders (ironSource, Vungle, Meta).
 *
 * @param enableAds Master toggle for all ads functionality
 * @param enableInterstitial Toggle for interstitial ads specifically
 * @param enableBanner Toggle for banner ads specifically
 * @param interstitialBlocklistRoutes Set of route names where interstitials should never show
 * @param showInterstitialBeforeNavigate If true: show before navigation, else after
 * @param enableBiddingOptimization Enable internal bidding-related optimizations/heuristics
 * @param enableMediationAnalytics Enable logging/analytics for mediation events
 * @param enableVungleBidding Enable Vungle (Liftoff Monetize) as a bidding partner
 * @param enableVungleLogging Enable verbose logging for Vungle bidder (analytics side only)
 * @param enableIronSourceBidding Enable ironSource as a bidding partner
 * @param enableIronSourceLogging Enable verbose logging for ironSource bidder (analytics side only)
 * @param enableMetaBidding Enable Meta Audience Network as a bidding partner
 * @param enableMetaLogging Enable verbose logging for Meta bidder (analytics side only)
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
    
    // Bidding mediation configuration (no app keys needed)
    val enableBiddingOptimization: Boolean = true,
    val enableMediationAnalytics: Boolean = true,
    
    // Vungle Liftoff Monetize bidding configuration
    val enableVungleBidding: Boolean = true,
    val enableVungleLogging: Boolean = true,
    // ironSource bidding configuration
    val enableIronSourceBidding: Boolean = true,
    val enableIronSourceLogging: Boolean = true,
    
    // Meta Audience Network bidding configuration
    val enableMetaBidding: Boolean = true,
    val enableMetaLogging: Boolean = true,
    
    // Banner refresh settings (for tracking AdMob auto-refresh)
    val trackBannerRefresh: Boolean = true,
    val enableBannerRefreshAnalytics: Boolean = true
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
     * Checks if banner refresh tracking is enabled
     * Note: Actual refresh is handled by AdMob Console settings
     */
    fun shouldTrackBannerRefresh(): Boolean = trackBannerRefresh
    
    /**
     * Checks if bidding mediation optimization is enabled
     */
    fun isBiddingOptimizationEnabled(): Boolean = enableBiddingOptimization
    
    /**
     * Checks if mediation analytics should be tracked
     */
    fun shouldTrackMediationAnalytics(): Boolean = enableMediationAnalytics
    
    /**
     * Checks if Vungle Liftoff bidding is enabled
     */
    fun isVungleBiddingEnabled(): Boolean = enableVungleBidding
    
    /**
     * Checks if Vungle logging should be enabled
     */
    fun shouldEnableVungleLogging(): Boolean = enableVungleLogging

    /**
     * Checks if ironSource bidding is enabled
     */
    fun isIronSourceBiddingEnabled(): Boolean = enableIronSourceBidding

    /**
     * Checks if ironSource logging should be enabled
     */
    fun shouldEnableIronSourceLogging(): Boolean = enableIronSourceLogging
    
    /**
     * Checks if Meta Audience Network bidding is enabled
     */
    fun isMetaBiddingEnabled(): Boolean = enableMetaBidding
    
    /**
     * Checks if Meta logging should be enabled
     */
    fun shouldEnableMetaLogging(): Boolean = enableMetaLogging
}