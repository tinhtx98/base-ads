/*
 * Base Ads Module - Analytics Logger
 * 
 * Interface for logging analytics events and Firebase Analytics implementation.
 * Provides hooks for tracking ads events and user interactions.
 */

package com.tinhtx.baseads.core

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for logging analytics events.
 * Apps can implement custom analytics providers or use the default Firebase implementation.
 */
interface AnalyticsLogger {
    
    /**
     * Logs an event with optional parameters
     * 
     * @param name Event name
     * @param params Event parameters
     */
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    
    /**
     * Sets the current screen name for analytics tracking
     * 
     * @param screen Screen name
     */
    fun setCurrentScreen(screen: String)
}

/**
 * Firebase Analytics implementation of AnalyticsLogger.
 * Automatically logs events to Firebase Analytics with proper parameter conversion.
 */
@Singleton
class FirebaseAnalyticsLogger @Inject constructor() : AnalyticsLogger {
    
    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }
    
    override fun logEvent(name: String, params: Map<String, Any?>) {
        analytics.logEvent(name) {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Int -> param(key, value.toLong())
                    is Long -> param(key, value)
                    is Float -> param(key, value.toDouble())
                    is Double -> param(key, value)
                    is Boolean -> param(key, if (value) "true" else "false")
                    null -> param(key, "null")
                    else -> param(key, value.toString())
                }
            }
        }
        
        // Log to debug console if enabled
        AdsLogger.d("Analytics", "Event: $name, Params: $params")
    }
    
    override fun setCurrentScreen(screen: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screen)
        }
        
        AdsLogger.d("Analytics", "Screen: $screen")
    }
}

/**
 * No-op implementation for testing or when analytics is disabled
 */
class NoOpAnalyticsLogger : AnalyticsLogger {
    override fun logEvent(name: String, params: Map<String, Any?>) {
        // No operation
    }
    
    override fun setCurrentScreen(screen: String) {
        // No operation
    }
}