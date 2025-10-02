/*
 * Force Update - UI State
 * 
 * State model for force update dialog UI
 */

package com.tinhtx.baseads.forceupdate.presentation

/**
 * UI state for force update dialog
 * 
 * @param shouldForceUpdate Whether to show the force update dialog
 * @param title Dialog title (already resolved with fallback)
 * @param message Dialog message (already resolved with fallback)
 * @param storeUrl Play Store URL to open (null = auto-generate)
 */
data class ForceUpdateState(
    val shouldForceUpdate: Boolean = false,
    val title: String = "",
    val message: String = "",
    val storeUrl: String? = null
)
