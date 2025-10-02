/*
 * Force Update - Use Case
 * 
 * Business logic for evaluating whether to force update based on remote config.
 * Checks two conditions: force_now flag OR version code mismatch.
 */

package com.tinhtx.baseads.forceupdate.domain

import javax.inject.Inject

/**
 * Use case to evaluate if app should force update
 * 
 * Logic:
 * 1. If config.forceNow == true → Force update immediately
 * 2. If config.latestVersionCode != appVersionCode → Force update
 * 3. Otherwise → No force update needed
 */
class EvaluateForceUpdateUseCase @Inject constructor() {
    
    /**
     * Evaluate if force update is required
     * 
     * @param config The remote config for force update
     * @param appVersionCode Current app version code from BuildConfig
     * @return true if app should show force update dialog
     */
    operator fun invoke(config: ForceUpdateConfig, appVersionCode: Int): Boolean {
        // Priority 1: Check force_now flag
        if (config.forceNow) {
            return true
        }
        
        // Priority 2: Check version code mismatch
        val latestVersion = config.latestVersionCode
        if (latestVersion != null && latestVersion != appVersionCode) {
            return true
        }
        
        // No force update needed
        return false
    }
}
