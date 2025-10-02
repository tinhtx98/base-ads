/*
 * Force Update - Remote Config Data Source
 * 
 * Handles Firebase Remote Config operations:
 * - Fetch and activate config
 * - Parse JSON configuration
 * - Real-time config updates listener
 */

package com.tinhtx.baseads.forceupdate.data

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.tinhtx.baseads.forceupdate.domain.ForceUpdateConfig
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for Firebase Remote Config
 * Handles fetching, parsing, and listening to force_update_json parameter
 */
@Singleton
class RemoteConfigDataSource @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {
    
    companion object {
        private const val TAG = "RemoteConfigDS"
        private const val KEY_FORCE_UPDATE_JSON = "force_update_json"
        
        // JSON keys
        private const val KEY_LATEST_VERSION_CODE = "latest_version_code"
        private const val KEY_LATEST_VERSION_NAME = "latest_version_name"
        private const val KEY_FORCE_NOW = "force_now"
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_STORE_URL = "store_url"
    }
    
    /**
     * Fetch and activate remote config
     * @return Parsed ForceUpdateConfig or default if fetch fails
     */
    suspend fun fetchAndActivate(): ForceUpdateConfig {
        return try {
            Log.d(TAG, "Fetching remote config...")
            val success = remoteConfig.fetchAndActivate().await()
            
            if (success) {
                Log.d(TAG, "Remote config fetched and activated successfully")
            } else {
                Log.d(TAG, "Remote config fetch completed but no new values")
            }
            
            parseConfig()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch remote config", e)
            ForceUpdateConfig.default()
        }
    }
    
    /**
     * Parse current remote config value
     * @return Parsed ForceUpdateConfig or default if parsing fails
     */
    fun parseConfig(): ForceUpdateConfig {
        return try {
            val jsonString = remoteConfig.getString(KEY_FORCE_UPDATE_JSON)
            
            if (jsonString.isBlank()) {
                Log.d(TAG, "force_update_json is empty, using default")
                return ForceUpdateConfig.default()
            }
            
            Log.d(TAG, "Parsing force_update_json: $jsonString")
            parseJsonToConfig(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse force_update_json", e)
            ForceUpdateConfig.default()
        }
    }
    
    /**
     * Add realtime listener for config updates
     * @param onChanged Callback when config changes
     */
    fun addRealtimeListener(onChanged: (ForceUpdateConfig) -> Unit) {
        remoteConfig.addOnConfigUpdateListener(object : com.google.firebase.remoteconfig.ConfigUpdateListener {
            override fun onUpdate(configUpdate: com.google.firebase.remoteconfig.ConfigUpdate) {
                Log.d(TAG, "Remote config updated, activating...")
                
                remoteConfig.activate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Remote config activated after update")
                        val config = parseConfig()
                        onChanged(config)
                    } else {
                        Log.e(TAG, "Failed to activate remote config after update")
                    }
                }
            }
            
            override fun onError(error: com.google.firebase.remoteconfig.FirebaseRemoteConfigException) {
                Log.e(TAG, "Remote config update error", error)
            }
        })
    }
    
    /**
     * Parse JSON string to ForceUpdateConfig
     * Handles null values and missing fields gracefully
     */
    private fun parseJsonToConfig(jsonString: String): ForceUpdateConfig {
        val json = JSONObject(jsonString)
        
        return ForceUpdateConfig(
            latestVersionCode = if (json.has(KEY_LATEST_VERSION_CODE) && !json.isNull(KEY_LATEST_VERSION_CODE)) {
                json.getInt(KEY_LATEST_VERSION_CODE)
            } else null,
            
            latestVersionName = if (json.has(KEY_LATEST_VERSION_NAME) && !json.isNull(KEY_LATEST_VERSION_NAME)) {
                json.getString(KEY_LATEST_VERSION_NAME)
            } else null,
            
            forceNow = json.optBoolean(KEY_FORCE_NOW, false),
            
            title = if (json.has(KEY_TITLE) && !json.isNull(KEY_TITLE)) {
                json.getString(KEY_TITLE).takeIf { it.isNotBlank() }
            } else null,
            
            message = if (json.has(KEY_MESSAGE) && !json.isNull(KEY_MESSAGE)) {
                json.getString(KEY_MESSAGE).takeIf { it.isNotBlank() }
            } else null,
            
            storeUrl = if (json.has(KEY_STORE_URL) && !json.isNull(KEY_STORE_URL)) {
                json.getString(KEY_STORE_URL).takeIf { it.isNotBlank() }
            } else null
        )
    }
}
