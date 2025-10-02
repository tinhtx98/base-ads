/*
 * Force Update - Repository
 * 
 * Repository layer for force update functionality.
 * Manages config state and provides Flow for UI observation.
 */

package com.tinhtx.baseads.forceupdate.data

import android.util.Log
import com.tinhtx.baseads.forceupdate.domain.ForceUpdateConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for force update configuration
 * 
 * Provides:
 * - StateFlow for config observation
 * - Initialization with fetch and activate
 * - Realtime updates listener
 */
@Singleton
class ForceUpdateRepository @Inject constructor(
    private val dataSource: RemoteConfigDataSource
) {
    companion object {
        private const val TAG = "ForceUpdateRepo"
    }
    
    // Repository scope for background operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // State flow for config
    private val _configFlow = MutableStateFlow(ForceUpdateConfig.default())
    val configFlow: StateFlow<ForceUpdateConfig> = _configFlow.asStateFlow()
    
    // Flag to ensure initialization happens once
    private var isInitialized = false
    
    /**
     * Initialize repository
     * - Fetch and activate remote config
     * - Set up realtime listener
     * 
     * Call this from Application.onCreate()
     */
    fun init() {
        if (isInitialized) {
            Log.d(TAG, "Already initialized, skipping")
            return
        }
        
        isInitialized = true
        Log.d(TAG, "Initializing force update repository")
        
        // Fetch and activate config
        scope.launch {
            val config = dataSource.fetchAndActivate()
            _configFlow.value = config
            Log.d(TAG, "Initial config loaded: forceNow=${config.forceNow}, latestVersionCode=${config.latestVersionCode}")
        }
        
        // Setup realtime listener for config updates
        dataSource.addRealtimeListener { updatedConfig ->
            Log.d(TAG, "Config updated via realtime: forceNow=${updatedConfig.forceNow}, latestVersionCode=${updatedConfig.latestVersionCode}")
            _configFlow.value = updatedConfig
        }
    }
    
    /**
     * Manually refresh config
     * Useful when app resumes from background
     */
    suspend fun refresh() {
        Log.d(TAG, "Manually refreshing config")
        val config = dataSource.fetchAndActivate()
        _configFlow.value = config
    }
}
