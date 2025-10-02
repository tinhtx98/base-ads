/*
 * Force Update - ViewModel
 * 
 * ViewModel for managing force update state and business logic.
 * Observes remote config and evaluates force update conditions.
 */

package com.tinhtx.baseads.forceupdate.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.baseads.BuildConfig
import com.tinhtx.baseads.R
import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.forceupdate.data.ForceUpdateRepository
import com.tinhtx.baseads.forceupdate.domain.EvaluateForceUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for force update feature
 * 
 * Responsibilities:
 * - Observe remote config changes
 * - Evaluate force update conditions
 * - Provide UI state with resolved title/message
 * - Track if dialog was already shown in this session
 */
@HiltViewModel
class ForceUpdateViewModel @Inject constructor(
    private val repository: ForceUpdateRepository,
    private val evaluateForceUpdate: EvaluateForceUpdateUseCase,
    private val application: Application,
    private val analyticsLogger: AnalyticsLogger
) : ViewModel() {
    
    companion object {
        private const val TAG = "ForceUpdateVM"
    }
    
    // UI state flow
    private val _state = MutableStateFlow(ForceUpdateState())
    val state: StateFlow<ForceUpdateState> = _state.asStateFlow()
    
    // Session flag to track if user clicked update (prevent spam)
    private var userClickedUpdate = false
    
    init {
        Log.d(TAG, "ViewModel initialized, current version code: ${BuildConfig.VERSION_CODE}")
        observeConfig()
    }
    
    /**
     * Observe remote config changes and update state
     */
    private fun observeConfig() {
        viewModelScope.launch {
            repository.configFlow.collect { config ->
                Log.d(TAG, "Config received: forceNow=${config.forceNow}, latestVersion=${config.latestVersionCode}")
                
                // Evaluate if force update is needed
                val shouldForce = evaluateForceUpdate(config, BuildConfig.VERSION_CODE)
                
                Log.d(TAG, "Should force update: $shouldForce")
                
                // Only show if needed and user hasn't already clicked update in this session
                val shouldShow = shouldForce && !userClickedUpdate
                
                if (shouldShow && !_state.value.shouldForceUpdate) {
                    // Log analytics event when showing dialog
                    analyticsLogger.logEvent("force_update_shown", mapOf(
                        "latest_version_code" to (config.latestVersionCode ?: -1),
                        "current_version_code" to BuildConfig.VERSION_CODE,
                        "force_now" to config.forceNow
                    ))
                }
                
                // Update state with resolved values
                _state.value = ForceUpdateState(
                    shouldForceUpdate = shouldShow,
                    title = resolveTitle(config.title),
                    message = resolveMessage(config.message),
                    storeUrl = config.storeUrl
                )
            }
        }
    }
    
    /**
     * Handle user clicking update button
     */
    fun onUpdateClicked() {
        Log.d(TAG, "User clicked update button")
        
        // Mark that user clicked to prevent re-showing in this session
        userClickedUpdate = true
        
        // Log analytics
        analyticsLogger.logEvent("force_update_click", mapOf(
            "version_code" to BuildConfig.VERSION_CODE
        ))
        
        // Hide dialog
        _state.value = _state.value.copy(shouldForceUpdate = false)
    }
    
    /**
     * Manually refresh config (call from onResume)
     */
    fun refresh() {
        Log.d(TAG, "Manually refreshing config")
        viewModelScope.launch {
            repository.refresh()
        }
    }
    
    /**
     * Resolve title with fallback to default string resource
     */
    private fun resolveTitle(remoteTitle: String?): String {
        return if (!remoteTitle.isNullOrBlank()) {
            remoteTitle
        } else {
            application.getString(R.string.force_update_title_default)
        }
    }
    
    /**
     * Resolve message with fallback to default string resource
     */
    private fun resolveMessage(remoteMessage: String?): String {
        return if (!remoteMessage.isNullOrBlank()) {
            remoteMessage
        } else {
            application.getString(R.string.force_update_message_default)
        }
    }
}
