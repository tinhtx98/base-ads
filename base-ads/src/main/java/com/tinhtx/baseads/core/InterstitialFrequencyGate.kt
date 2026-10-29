/*
 * Base Ads Module - Interstitial Frequency Gate
 *
 * Pure (Android-free) bookkeeping for the interstitial frequency policy:
 * action threshold, duplicate-trigger debounce, per-process cap and the
 * once-per-process splash claim. Kept separate from InterstitialAdManager so
 * it can be unit tested on the JVM.
 */

package com.tinhtx.baseads.core

/**
 * State is held in memory, so its lifetime is the process lifetime
 * (InterstitialAdManager is a Hilt singleton).
 *
 * @param actionThreshold User actions required before an interstitial may show.
 *   A value of 1 or less disables counting (every action is eligible).
 * @param dedupeMillis Actions arriving within this window of the previous one are
 *   treated as the same user gesture and are neither counted nor eligible.
 * @param maxPerSession Maximum interstitials per process, splash included.
 */
internal class InterstitialFrequencyGate(
    private val actionThreshold: Int,
    private val dedupeMillis: Long,
    private val maxPerSession: Int
) {

    private var actionCount = 0
    private var lastActionAtMs = Long.MIN_VALUE
    private var shownInSession = 0
    private var splashClaimed = false

    /**
     * Registers one user action.
     * @return true when this action is eligible to show an interstitial.
     */
    @Synchronized
    fun registerAction(nowMs: Long): Boolean {
        if (actionThreshold <= 1) return true

        val isDuplicate = lastActionAtMs != Long.MIN_VALUE && nowMs - lastActionAtMs < dedupeMillis
        lastActionAtMs = nowMs
        if (isDuplicate) return false

        actionCount++
        return actionCount >= actionThreshold
    }

    /** Call when an interstitial has been dismissed. */
    @Synchronized
    fun onAdShown() {
        shownInSession++
        actionCount = 0
    }

    @Synchronized
    fun isSessionCapReached(): Boolean = shownInSession >= maxPerSession

    /**
     * Claims the single splash slot of this process.
     * @return true only for the first caller.
     */
    @Synchronized
    fun tryClaimSplash(): Boolean {
        if (splashClaimed) return false
        splashClaimed = true
        return true
    }

    @Synchronized
    fun debugInfo(): Map<String, Any> = mapOf(
        "action_count" to actionCount,
        "action_threshold" to actionThreshold,
        "shown_in_session" to shownInSession,
        "max_per_session" to maxPerSession,
        "splash_claimed" to splashClaimed
    )
}
