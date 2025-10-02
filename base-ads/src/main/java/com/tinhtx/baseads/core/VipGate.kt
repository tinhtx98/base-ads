/*
 * Base Ads Module - VIP Gate
 * 
 * Interface for checking VIP status to disable ads for premium users.
 * Apps should implement this to integrate with their VIP/subscription system.
 */

package com.tinhtx.baseads.core

import com.tinhtx.baseads.data.AdsPrefs
import javax.inject.Inject

/**
 * Interface for checking if the current user has VIP/premium status.
 * When a user is VIP, all ads should be disabled.
 */
interface VipGate {
    
    /**
     * Returns true if the current user has VIP/premium status
     * and should not see any ads.
     */
    fun isVip(): Boolean
}

/**
 * Default implementation that always returns false.
 * Apps should replace this with their actual VIP checking logic.
 */
/**
 * Default implementation of VipGate using shared preferences.
 * Can be used as a fallback or simple test implementation.
 */
class DefaultVipGate @Inject constructor(
    private val adsPrefs: AdsPrefs
) : VipGate {
    override fun isVip(): Boolean = false
}

/**
 * Test implementation for debugging and testing.
 * Allows toggling VIP status for testing ad disable functionality.
 */
class TestVipGate(private var vipStatus: Boolean = false) : VipGate {
    
    override fun isVip(): Boolean = vipStatus
    
    /**
     * Sets VIP status for testing purposes
     */
    fun setVipStatus(isVip: Boolean) {
        vipStatus = isVip
    }
}