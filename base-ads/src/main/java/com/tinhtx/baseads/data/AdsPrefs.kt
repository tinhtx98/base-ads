/*
 * Base Ads Module - Ads Preferences
 * 
 * Manages SharedPreferences for ads module including cooldown tracking,
 * daily caps, and session state.
 */

package com.tinhtx.baseads.data

import android.content.Context
import android.content.SharedPreferences
import com.tinhtx.baseads.core.AdsConstants
import com.tinhtx.baseads.core.AdsLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages SharedPreferences for the ads module.
 * Handles cooldown tracking, daily caps, and session state persistence.
 */
@Singleton
class AdsPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(AdsConstants.PREF_NAMESPACE, Context.MODE_PRIVATE)
    }
    
    companion object {
        private const val KEY_LAST_INTERSTITIAL_EPOCH = "last_interstitial_epoch"
        private const val KEY_TODAY_INTERSTITIAL_COUNT = "today_interstitial_count"
        private const val KEY_LAST_COUNT_RESET_DAY = "last_count_reset_day"
        private const val KEY_APP_LAUNCH_COUNT = "app_launch_count"
        private const val KEY_TOTAL_INTERSTITIAL_SHOWN = "total_interstitial_shown"
    }
    
    /**
     * Gets the timestamp (in seconds) when interstitial was last shown
     */
    fun getLastInterstitialEpoch(): Long {
        return prefs.getLong(KEY_LAST_INTERSTITIAL_EPOCH, 0)
    }
    
    /**
     * Sets the timestamp when interstitial was shown
     */
    fun setLastInterstitialEpoch(epochSeconds: Long) {
        prefs.edit()
            .putLong(KEY_LAST_INTERSTITIAL_EPOCH, epochSeconds)
            .apply()
        
        AdsLogger.d("Prefs", "Last interstitial epoch set: $epochSeconds")
    }
    
    /**
     * Gets today's interstitial count, automatically resets if new day
     */
    fun getTodayInterstitialCount(): Int {
        resetTodayIfNeeded()
        return prefs.getInt(KEY_TODAY_INTERSTITIAL_COUNT, 0)
    }
    
    /**
     * Increments today's interstitial count
     */
    fun incrementTodayInterstitialCount() {
        resetTodayIfNeeded()
        val currentCount = prefs.getInt(KEY_TODAY_INTERSTITIAL_COUNT, 0)
        val newCount = currentCount + 1
        
        prefs.edit()
            .putInt(KEY_TODAY_INTERSTITIAL_COUNT, newCount)
            .apply()
        
        // Also increment total count
        incrementTotalInterstitialShown()
        
        AdsLogger.d("Prefs", "Today's interstitial count incremented: $newCount")
    }
    
    /**
     * Gets total number of interstitials shown across all time
     */
    fun getTotalInterstitialShown(): Int {
        return prefs.getInt(KEY_TOTAL_INTERSTITIAL_SHOWN, 0)
    }
    
    /**
     * Increments total interstitial shown counter
     */
    private fun incrementTotalInterstitialShown() {
        val total = getTotalInterstitialShown() + 1
        prefs.edit()
            .putInt(KEY_TOTAL_INTERSTITIAL_SHOWN, total)
            .apply()
    }
    
    /**
     * Gets app launch count
     */
    fun getAppLaunchCount(): Int {
        return prefs.getInt(KEY_APP_LAUNCH_COUNT, 0)
    }
    
    /**
     * Increments app launch count
     */
    fun incrementAppLaunchCount() {
        val currentCount = getAppLaunchCount()
        val newCount = currentCount + 1
        
        prefs.edit()
            .putInt(KEY_APP_LAUNCH_COUNT, newCount)
            .apply()
        
        AdsLogger.d("Prefs", "App launch count incremented: $newCount")
    }
    
    /**
     * Resets today's count if it's a new day
     */
    private fun resetTodayIfNeeded() {
        val today = getTodayKey()
        val lastResetDay = prefs.getInt(KEY_LAST_COUNT_RESET_DAY, 0)
        
        if (today != lastResetDay) {
            AdsLogger.d("Prefs", "New day detected, resetting today's count. Day: $today")
            
            prefs.edit()
                .putInt(KEY_TODAY_INTERSTITIAL_COUNT, 0)
                .putInt(KEY_LAST_COUNT_RESET_DAY, today)
                .apply()
        }
    }
    
    /**
     * Gets current day as key (days since epoch)
     */
    private fun getTodayKey(): Int {
        val currentTimeMillis = System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(currentTimeMillis).toInt()
    }
    
    /**
     * Clears all ads preferences (useful for testing)
     */
    fun clearAll() {
        prefs.edit().clear().apply()
        AdsLogger.d("Prefs", "All ads preferences cleared")
    }
    
    /**
     * Gets debug info about current preferences state
     */
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "last_interstitial_epoch" to getLastInterstitialEpoch(),
            "today_interstitial_count" to getTodayInterstitialCount(),
            "total_interstitial_shown" to getTotalInterstitialShown(),
            "app_launch_count" to getAppLaunchCount(),
            "current_day_key" to getTodayKey()
        )
    }
}