/*
 * Base Ads Module - Open App Ad Manager
 * 
 * Manages open app ads lifecycle including loading, caching, and showing
 * when the app comes to foreground from background.
 */

package com.tinhtx.baseads.openapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.tinhtx.baseads.core.AdRevenueReporter
import com.tinhtx.baseads.core.AdUnitsProvider
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdsConstants
import com.tinhtx.baseads.core.AdsLogger
import com.tinhtx.baseads.core.AdStatus
import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.core.VipGate
import com.tinhtx.baseads.data.AdsPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result sealed class for open app ad operations.
 */
sealed class OpenAppAdResult {
    data object Shown : OpenAppAdResult()
    data object NoAdAvailable : OpenAppAdResult()
    data class Error(val message: String) : OpenAppAdResult()
    data object CooldownActive : OpenAppAdResult()
    data object VipUser : OpenAppAdResult()
    data object Disabled : OpenAppAdResult()
    data object ExcludedActivity : OpenAppAdResult()
}

/**
 * Manages open app ads with automatic foreground detection.
 * 
 * Features:
 * - Automatic loading and caching
 * - Shows ad when app comes from background to foreground
 * - Configurable cooldown between shows
 * - Activity exclusion list
 * - ILRD tracking
 * - VIP gate integration
 * 
 * Usage:
 * ```kotlin
 * // In Application.onCreate()
 * @Inject lateinit var openAppAdManager: OpenAppAdManager
 * 
 * override fun onCreate() {
 *     super.onCreate()
 *     openAppAdManager.initialize(this)
 * }
 * ```
 */
@Singleton
class OpenAppAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnitsProvider: AdUnitsProvider,
    private val adsConfig: AdsConfig,
    private val vipGate: VipGate,
    private val analyticsLogger: AnalyticsLogger,
    private val adRevenueReporter: AdRevenueReporter,
    private val adsPrefs: AdsPrefs
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {
    
    companion object {
        private const val TAG = "OpenAppAd"
        private const val AD_ID = "open_app_main"
    }
    
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = AtomicBoolean(false)
    private var isShowingAd = AtomicBoolean(false)
    private var loadTimeMs: Long = 0
    private var lastShowTimeMs = AtomicLong(0)
    
    // Current activity tracking
    private var currentActivity: Activity? = null
    
    // Initialization flag
    private var isInitialized = false
    
    // Callback for when ad is dismissed
    private var onAdDismissedCallback: (() -> Unit)? = null
    
    // Excluded activities (won't show ad on these)
    private val excludedActivities = mutableSetOf<String>()
    
    /**
     * Initializes the open app ad manager.
     * Must be called in Application.onCreate().
     * 
     * @param application The application instance
     * @param excludeActivities List of activity class names to exclude from showing ads
     */
    fun initialize(
        application: Application,
        excludeActivities: List<String> = emptyList()
    ) {
        if (isInitialized) {
            AdsLogger.w(TAG, "Already initialized, skipping")
            return
        }
        
        AdsLogger.i(TAG, "Initializing Open App Ad Manager")
        
        // Add excluded activities
        excludedActivities.addAll(excludeActivities)
        excludedActivities.addAll(adsConfig.openAppExcludedActivities)
        
        // Register lifecycle observers
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        application.registerActivityLifecycleCallbacks(this)
        
        isInitialized = true
        
        // Preload first ad
        loadAd()
        
        analyticsLogger.logEvent("open_app_ad_init", mapOf(
            "excluded_activities_count" to excludedActivities.size
        ))
    }
    
    /**
     * Adds an activity to the exclusion list.
     * Ads won't show when this activity is in foreground.
     */
    fun excludeActivity(activityClassName: String) {
        excludedActivities.add(activityClassName)
    }
    
    /**
     * Removes an activity from the exclusion list.
     */
    fun includeActivity(activityClassName: String) {
        excludedActivities.remove(activityClassName)
    }
    
    /**
     * Sets callback to be invoked when ad is dismissed.
     */
    fun setOnAdDismissedCallback(callback: () -> Unit) {
        onAdDismissedCallback = callback
    }
    
    /**
     * Manually shows an open app ad if available.
     * Bypasses the automatic foreground detection.
     * 
     * @param activity Activity to show the ad on
     * @param bypassCooldown If true, ignores cooldown restriction
     * @return Result of the show attempt
     */
    fun showIfAvailable(
        activity: Activity,
        bypassCooldown: Boolean = false
    ): OpenAppAdResult {
        // Check VIP
        if (vipGate.isVip()) {
            AdsLogger.d(TAG, "Skipping show - VIP user")
            return OpenAppAdResult.VipUser
        }
        
        // Check if enabled
        if (!adsConfig.shouldShowOpenAppAd()) {
            AdsLogger.d(TAG, "Skipping show - Open app ads disabled")
            return OpenAppAdResult.Disabled
        }
        
        // Check excluded activity
        val activityName = activity.javaClass.simpleName
        if (excludedActivities.contains(activityName)) {
            AdsLogger.d(TAG, "Skipping show - Activity excluded: $activityName")
            return OpenAppAdResult.ExcludedActivity
        }
        
        // Check cooldown
        if (!bypassCooldown && !isCooldownPassed()) {
            val remaining = getCooldownRemaining()
            AdsLogger.d(TAG, "Skipping show - Cooldown active: ${remaining}s remaining")
            return OpenAppAdResult.CooldownActive
        }
        
        // Check daily cap
        if (adsPrefs.getTodayOpenAppCount() >= AdsConstants.OPEN_APP_DAILY_CAP) {
            AdsLogger.d(TAG, "Skipping show - Daily cap reached")
            return OpenAppAdResult.NoAdAvailable
        }
        
        // Check if already showing
        if (isShowingAd.get()) {
            AdsLogger.d(TAG, "Skipping show - Already showing")
            return OpenAppAdResult.NoAdAvailable
        }
        
        // Check if ad is available
        val ad = appOpenAd
        if (ad == null || isAdExpired()) {
            AdsLogger.d(TAG, "No ad available or expired, loading new one")
            loadAd()
            return OpenAppAdResult.NoAdAvailable
        }
        
        // Show the ad
        return showAd(activity, ad)
    }
    
    /**
     * Loads an open app ad.
     */
    fun loadAd() {
        if (vipGate.isVip() || !adsConfig.shouldShowOpenAppAd()) {
            AdsLogger.d(TAG, "Skipping load - VIP or disabled")
            return
        }
        
        if (isLoadingAd.getAndSet(true)) {
            AdsLogger.d(TAG, "Skipping load - Already loading")
            return
        }
        
        if (appOpenAd != null && !isAdExpired()) {
            AdsLogger.d(TAG, "Skipping load - Valid ad already cached")
            isLoadingAd.set(false)
            return
        }
        
        AdsLogger.d(TAG, "Loading open app ad")
        AdsLogger.registerAd(AD_ID, "open_app")
        AdsLogger.updateAdStatus(AD_ID, AdStatus.INITIALIZING)
        AdsLogger.markOpenAppRequest()
        
        val request = AdRequest.Builder().build()
        
        AppOpenAd.load(
            context,
            adUnitsProvider.openAppAdUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTimeMs = System.currentTimeMillis()
                    isLoadingAd.set(false)
                    
                    AdsLogger.updateAdStatus(AD_ID, AdStatus.READY)
                    AdsLogger.d(TAG, "Open app ad loaded")
                    
                    // Attach ILRD listener
                    ad.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                        val responseInfo = ad.responseInfo
                        val adapterClassName = responseInfo?.loadedAdapterResponseInfo?.adapterClassName
                        
                        adRevenueReporter.reportOpenAppRevenue(
                            adUnitsProvider.openAppAdUnitId,
                            adValue.valueMicros,
                            adValue.currencyCode,
                            adValue.precisionType,
                            adapterClassName = adapterClassName
                        )
                    }
                    
                    analyticsLogger.logEvent("ad_open_app_loaded", mapOf(
                        "ad_unit_id" to adUnitsProvider.openAppAdUnitId
                    ))
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd.set(false)
                    appOpenAd = null
                    
                    val errorMsg = "Code: ${loadAdError.code} - ${loadAdError.message}"
                    AdsLogger.updateAdStatus(AD_ID, AdStatus.FAILED, errorMsg)
                    AdsLogger.e(TAG, "Failed to load open app ad: $errorMsg")
                    
                    analyticsLogger.logEvent("ad_open_app_load_failed", mapOf(
                        "error_code" to loadAdError.code,
                        "error_message" to loadAdError.message,
                        "ad_unit_id" to adUnitsProvider.openAppAdUnitId
                    ))
                }
            }
        )
    }
    
    /**
     * Gets debug information about current state.
     */
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "is_initialized" to isInitialized,
            "has_cached_ad" to (appOpenAd != null),
            "is_loading" to isLoadingAd.get(),
            "is_showing" to isShowingAd.get(),
            "is_expired" to isAdExpired(),
            "cooldown_remaining_s" to getCooldownRemaining(),
            "today_count" to adsPrefs.getTodayOpenAppCount(),
            "daily_cap" to AdsConstants.OPEN_APP_DAILY_CAP,
            "excluded_activities" to excludedActivities.size,
            "vip_status" to vipGate.isVip(),
            "enabled" to adsConfig.shouldShowOpenAppAd()
        )
    }
    
    // ============================================================================
    // Lifecycle Observer Methods
    // ============================================================================
    
    override fun onStart(owner: LifecycleOwner) {
        // App came to foreground
        AdsLogger.d(TAG, "App came to foreground")
        
        currentActivity?.let { activity ->
            val result = showIfAvailable(activity)
            AdsLogger.d(TAG, "Auto show result: $result")
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        // App went to background
        AdsLogger.d(TAG, "App went to background")
    }
    
    // ============================================================================
    // Activity Lifecycle Callbacks
    // ============================================================================
    
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    
    override fun onActivityStarted(activity: Activity) {
        // Track current activity but don't set it immediately
        // This prevents showing ad on activity transitions within the app
    }
    
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    
    override fun onActivityPaused(activity: Activity) {}
    
    override fun onActivityStopped(activity: Activity) {}
    
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
    
    // ============================================================================
    // Private Methods
    // ============================================================================
    
    private fun showAd(activity: Activity, ad: AppOpenAd): OpenAppAdResult {
        isShowingAd.set(true)
        
        AdsLogger.d(TAG, "Showing open app ad")
        AdsLogger.updateAdStatus(AD_ID, AdStatus.SHOWING)
        
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                AdsLogger.d(TAG, "Open app ad dismissed")
                AdsLogger.updateAdStatus(AD_ID, AdStatus.DISMISSED)
                AdsLogger.markOpenAppImpression()
                
                appOpenAd = null
                isShowingAd.set(false)
                lastShowTimeMs.set(System.currentTimeMillis())
                
                // Update preferences
                adsPrefs.setLastOpenAppEpoch(System.currentTimeMillis() / 1000)
                adsPrefs.incrementTodayOpenAppCount()
                
                analyticsLogger.logEvent("ad_open_app_dismissed", mapOf(
                    "today_count" to adsPrefs.getTodayOpenAppCount()
                ))
                
                // Load next ad
                loadAd()
                
                // Invoke callback
                onAdDismissedCallback?.invoke()
                
                AdsLogger.logRateSummary()
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                val errorMsg = "Code: ${adError.code} - ${adError.message}"
                AdsLogger.e(TAG, "Failed to show open app ad: $errorMsg")
                AdsLogger.updateAdStatus(AD_ID, AdStatus.FAILED, errorMsg)
                
                appOpenAd = null
                isShowingAd.set(false)
                
                analyticsLogger.logEvent("ad_open_app_show_failed", mapOf(
                    "error_code" to adError.code,
                    "error_message" to adError.message
                ))
                
                // Load next ad
                loadAd()
            }
            
            override fun onAdShowedFullScreenContent() {
                AdsLogger.d(TAG, "Open app ad showed")
                
                analyticsLogger.logEvent("ad_open_app_impression", mapOf(
                    "ad_unit_id" to adUnitsProvider.openAppAdUnitId
                ))
            }
            
            override fun onAdClicked() {
                AdsLogger.d(TAG, "Open app ad clicked")
                
                analyticsLogger.logEvent("ad_open_app_clicked", mapOf(
                    "ad_unit_id" to adUnitsProvider.openAppAdUnitId
                ))
            }
        }
        
        ad.show(activity)
        return OpenAppAdResult.Shown
    }
    
    private fun isAdExpired(): Boolean {
        if (appOpenAd == null) return true
        
        val expirationMs = AdsConstants.OPEN_APP_EXPIRATION_HOURS * 60 * 60 * 1000
        val elapsed = System.currentTimeMillis() - loadTimeMs
        return elapsed > expirationMs
    }
    
    private fun isCooldownPassed(): Boolean {
        val lastShowEpoch = adsPrefs.getLastOpenAppEpoch()
        if (lastShowEpoch == 0L) return true
        
        val nowEpoch = System.currentTimeMillis() / 1000
        val elapsed = nowEpoch - lastShowEpoch
        return elapsed >= AdsConstants.OPEN_APP_COOLDOWN_SECONDS
    }
    
    private fun getCooldownRemaining(): Long {
        val lastShowEpoch = adsPrefs.getLastOpenAppEpoch()
        if (lastShowEpoch == 0L) return 0
        
        val nowEpoch = System.currentTimeMillis() / 1000
        val elapsed = nowEpoch - lastShowEpoch
        val remaining = AdsConstants.OPEN_APP_COOLDOWN_SECONDS - elapsed
        return if (remaining > 0) remaining else 0
    }
}
