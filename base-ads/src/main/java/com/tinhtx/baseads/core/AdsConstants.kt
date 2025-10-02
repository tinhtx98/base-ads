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
    
    /**
     * Test ad unit IDs provided by Google AdMob for development
     */
    object TestAdUnits {
        const val BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    }
}