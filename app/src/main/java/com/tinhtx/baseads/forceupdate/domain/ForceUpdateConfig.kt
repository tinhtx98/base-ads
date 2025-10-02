/*
 * Force Update - Domain Model
 * 
 * Data class representing the force update configuration from Firebase Remote Config.
 * Maps directly to the JSON structure from remote config parameter.
 */

package com.tinhtx.baseads.forceupdate.domain

/**
 * Force update configuration model
 * 
 * @param latestVersionCode The latest version code available on Play Store
 * @param latestVersionName The latest version name (for display purposes)
 * @param forceNow If true, force update regardless of version comparison
 * @param title Custom title for the update dialog (null = use default)
 * @param message Custom message for the update dialog (null = use default)
 * @param storeUrl Custom Play Store URL (null = auto-generate from package name)
 */
data class ForceUpdateConfig(
    val latestVersionCode: Int? = null,
    val latestVersionName: String? = null,
    val forceNow: Boolean = false,
    val title: String? = null,
    val message: String? = null,
    val storeUrl: String? = null
) {
    companion object {
        /**
         * Default config when no remote config is available or parsing fails
         */
        fun default() = ForceUpdateConfig(
            latestVersionCode = null,
            latestVersionName = null,
            forceNow = false,
            title = null,
            message = null,
            storeUrl = null
        )
    }
}
