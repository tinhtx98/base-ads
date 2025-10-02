/*
 * Force Update - Dependency Injection Module
 * 
 * Hilt module providing dependencies for force update feature.
 */

package com.tinhtx.baseads.forceupdate.di

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.tinhtx.baseads.BuildConfig
import com.tinhtx.baseads.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for force update dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object ForceUpdateModule {
    
    /**
     * Provide Firebase Remote Config instance
     * 
     * Configuration:
     * - Debug: fetch interval = 0 (immediate)
     * - Release: fetch interval = 3600s (1 hour)
     * - Sets defaults from RemoteConfigDefaults.json
     */
    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(
        @ApplicationContext context: Context
    ): FirebaseRemoteConfig {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        
        // Configure settings based on build type
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(
                if (BuildConfig.DEBUG) {
                    0L // Fetch immediately in debug
                } else {
                    3600L // 1 hour in release
                }
            )
            .build()
        
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // Set defaults from JSON file
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        
        return remoteConfig
    }
}
