/*
 * Base Ads Module - Ads Constants
 * 
 * Contains all constant values for ads behavior configuration
 * including cooldowns, caps, delays and preferences namespace.
 */

package com.tinhtx.baseads.core

/**
 * Constants for controlling ads display behavior to ensure policy compliance
 * and good user experience.
 */
object AdsConstants {
    
    /**
     * Minimum seconds between showing interstitial ads to prevent spam
     */
    const val INTERSTITIAL_COOLDOWN_SECONDS = 15L
    
    /**
     * Maximum number of interstitial ads to show per day
     */
    const val INTERSTITIAL_DAILY_CAP = 15
    
    /**
     * Delay before showing first interstitial after app launch
     */
    const val INTERSTITIAL_FIRST_LAUNCH_DELAY_SECONDS = 20L
    
    /**
     * Minimum seconds after opening a screen before showing interstitial
     * to avoid immediate interruption of user flow
     */
    const val MIN_SECONDS_AFTER_SCREEN_OPEN = 5L
    
    /**
     * SharedPreferences namespace for ads module
     */
    const val PREF_NAMESPACE = "base_ads_prefs"
    
    // ============================================================================
    // Open App Ad Constants
    // ============================================================================
    
    /**
     * Minimum seconds between showing open app ads (aggressive: 30s)
     */
    const val OPEN_APP_COOLDOWN_SECONDS = 30L
    
    /**
     * Maximum number of open app ads to show per day
     */
    const val OPEN_APP_DAILY_CAP = 50
    
    /**
     * Hours before an open app ad expires and needs reload
     */
    const val OPEN_APP_EXPIRATION_HOURS = 4L
    
    // ============================================================================
    // Native Ad Constants
    // ============================================================================
    
    /**
     * Maximum number of native ads to cache
     */
    const val NATIVE_AD_CACHE_SIZE = 5
    
    /**
     * Hours before a native ad expires and needs reload
     */
    const val NATIVE_AD_EXPIRATION_HOURS = 1L
    
    /**
     * Test ad unit IDs provided by Google AdMob for development
     */
    object TestAdUnits {
        const val BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val NATIVE = "ca-app-pub-3940256099942544/2247696110"
        const val NATIVE_VIDEO = "ca-app-pub-3940256099942544/1044960115"
        const val OPEN_APP = "ca-app-pub-3940256099942544/9257395921"
        const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
    }
}