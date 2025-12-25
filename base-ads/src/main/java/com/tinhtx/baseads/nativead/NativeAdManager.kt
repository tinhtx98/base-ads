/*
 * Base Ads Module - Native Ad Manager
 * 
 * Manages native ads lifecycle including loading, caching, and showing.
 * Supports multiple templates (inline small, medium, large height).
 */

package com.tinhtx.baseads.nativead

import android.content.Context
import android.util.LruCache
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.OnPaidEventListener
import com.tinhtx.baseads.core.AdUnitsProvider
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdsConstants
import com.tinhtx.baseads.core.AdsLogger
import com.tinhtx.baseads.core.AdStatus
import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.core.VipGate
import com.tinhtx.baseads.core.AdRevenueReporter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Cached native ad wrapper with timestamp for expiration checking.
 */
data class CachedNativeAd(
    val ad: NativeAd,
    val loadTimeMs: Long = System.currentTimeMillis(),
    val template: NativeAdTemplate
) {
    fun isExpired(): Boolean {
        val expirationMs = AdsConstants.NATIVE_AD_EXPIRATION_HOURS * 60 * 60 * 1000
        return System.currentTimeMillis() - loadTimeMs > expirationMs
    }
}

/**
 * Result sealed class for native ad loading operations.
 */
sealed class NativeAdResult {
    data class Success(val ad: NativeAd) : NativeAdResult()
    data class Error(val message: String, val code: Int) : NativeAdResult()
    data object VipUser : NativeAdResult()
    data object Disabled : NativeAdResult()
    data object Loading : NativeAdResult()
}

/**
 * Manages native ads with caching, preloading, and revenue tracking.
 * 
 * Features:
 * - LRU cache for loaded ads
 * - Template-based loading (inline small, medium, large)
 * - ILRD (Impression Level Revenue Data) tracking
 * - VIP gate integration
 * - Analytics logging
 */
@Singleton
class NativeAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnitsProvider: AdUnitsProvider,
    private val adsConfig: AdsConfig,
    private val vipGate: VipGate,
    private val analyticsLogger: AnalyticsLogger,
    private val adRevenueReporter: AdRevenueReporter
) {
    
    companion object {
        private const val TAG = "NativeAd"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // LRU cache for native ads - key is template name
    private val adCache = LruCache<String, CachedNativeAd>(AdsConstants.NATIVE_AD_CACHE_SIZE)
    
    // Track loading states per template
    private val loadingStates = ConcurrentHashMap<NativeAdTemplate, AtomicBoolean>()
    
    // Counter for tracking positions
    private val impressionCounter = AtomicInteger(0)
    
    // State flows for reactive updates
    private val _inlineSmallState = MutableStateFlow<NativeAdResult>(NativeAdResult.Loading)
    val inlineSmallState: StateFlow<NativeAdResult> = _inlineSmallState.asStateFlow()
    
    private val _inlineMediumState = MutableStateFlow<NativeAdResult>(NativeAdResult.Loading)
    val inlineMediumState: StateFlow<NativeAdResult> = _inlineMediumState.asStateFlow()
    
    private val _largeHeightState = MutableStateFlow<NativeAdResult>(NativeAdResult.Loading)
    val largeHeightState: StateFlow<NativeAdResult> = _largeHeightState.asStateFlow()
    
    init {
        // Initialize loading states
        NativeAdTemplate.entries.forEach { template ->
            loadingStates[template] = AtomicBoolean(false)
        }
    }
    
    /**
     * Preloads native ads for all templates.
     * Call this early (e.g., in Application.onCreate or MainActivity.onCreate).
     */
    fun preloadAll() {
        if (vipGate.isVip() || !adsConfig.shouldShowNativeAd()) {
            AdsLogger.d(TAG, "Skipping preload - VIP or native ads disabled")
            updateAllStates(
                if (vipGate.isVip()) NativeAdResult.VipUser else NativeAdResult.Disabled
            )
            return
        }
        
        AdsLogger.d(TAG, "Preloading all native ad templates")
        
        NativeAdTemplate.entries.forEach { template ->
            preload(template)
        }
    }
    
    /**
     * Preloads a native ad for the specified template.
     */
    fun preload(template: NativeAdTemplate) {
        if (vipGate.isVip() || !adsConfig.shouldShowNativeAd()) {
            return
        }
        
        val cacheKey = getCacheKey(template)
        
        // Check if already cached and not expired
        adCache.get(cacheKey)?.let { cached ->
            if (!cached.isExpired()) {
                AdsLogger.d(TAG, "[$template] Already cached and valid")
                updateState(template, NativeAdResult.Success(cached.ad))
                return
            } else {
                // Remove expired ad
                adCache.remove(cacheKey)
            }
        }
        
        // Check if already loading
        if (loadingStates[template]?.getAndSet(true) == true) {
            AdsLogger.d(TAG, "[$template] Already loading")
            return
        }
        
        AdsLogger.d(TAG, "[$template] Starting preload")
        updateState(template, NativeAdResult.Loading)
        
        loadNativeAd(template) { result ->
            loadingStates[template]?.set(false)
            updateState(template, result)
            
            if (result is NativeAdResult.Success) {
                adCache.put(cacheKey, CachedNativeAd(result.ad, template = template))
                AdsLogger.d(TAG, "[$template] Cached successfully")
            }
        }
    }
    
    /**
     * Gets a native ad for the specified template.
     * Returns cached ad if available, otherwise loads a new one.
     * 
     * @param template The native ad template type
     * @return NativeAdResult containing the ad or error
     */
    suspend fun getAd(template: NativeAdTemplate): NativeAdResult {
        if (vipGate.isVip()) {
            return NativeAdResult.VipUser
        }
        
        if (!adsConfig.shouldShowNativeAd()) {
            return NativeAdResult.Disabled
        }
        
        val cacheKey = getCacheKey(template)
        
        // Check cache first
        adCache.get(cacheKey)?.let { cached ->
            if (!cached.isExpired()) {
                AdsLogger.d(TAG, "[$template] Returning cached ad")
                
                // Remove from cache (one-time use)
                adCache.remove(cacheKey)
                
                // Preload next ad
                preload(template)
                
                return NativeAdResult.Success(cached.ad)
            } else {
                adCache.remove(cacheKey)
            }
        }
        
        // Load synchronously
        return loadNativeAdSuspend(template)
    }
    
    /**
     * Gets a native ad synchronously from cache only.
     * Does not trigger new load if not cached.
     * 
     * @param template The native ad template type
     * @return NativeAd if cached and valid, null otherwise
     */
    fun getCachedAd(template: NativeAdTemplate): NativeAd? {
        if (vipGate.isVip() || !adsConfig.shouldShowNativeAd()) {
            return null
        }
        
        val cacheKey = getCacheKey(template)
        val cached = adCache.get(cacheKey) ?: return null
        
        if (cached.isExpired()) {
            adCache.remove(cacheKey)
            preload(template)
            return null
        }
        
        // Remove from cache (one-time use)
        adCache.remove(cacheKey)
        
        // Preload next ad
        preload(template)
        
        return cached.ad
    }
    
    /**
     * Reports impression for revenue tracking.
     * Call this when native ad becomes visible.
     * 
     * @param ad The native ad being shown
     * @param template The template type
     * @param position Position in list/feed (for analytics)
     * @param route Current screen/route name
     */
    fun reportImpression(
        ad: NativeAd,
        template: NativeAdTemplate,
        position: Int = -1,
        route: String? = null
    ) {
        val impressionIndex = impressionCounter.incrementAndGet()
        
        AdsLogger.d(TAG, "[$template] Impression #$impressionIndex at position $position")
        AdsLogger.markNativeImpression()
        
        analyticsLogger.logEvent("ad_native_impression", mapOf(
            "template" to template.name,
            "position" to position,
            "route" to (route ?: "unknown"),
            "impression_index" to impressionIndex,
            "ad_unit_id" to adUnitsProvider.nativeAdUnitId
        ))
    }
    
    /**
     * Clears all cached ads.
     */
    fun clearCache() {
        // Destroy all cached ads
        for (i in 0 until adCache.size()) {
            val key = adCache.snapshot().keys.firstOrNull() ?: break
            adCache.remove(key)?.ad?.destroy()
        }
        
        AdsLogger.d(TAG, "Cache cleared")
    }
    
    /**
     * Gets debug information about current state.
     */
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "cache_size" to adCache.size(),
            "max_cache_size" to AdsConstants.NATIVE_AD_CACHE_SIZE,
            "impression_count" to impressionCounter.get(),
            "vip_status" to vipGate.isVip(),
            "native_enabled" to adsConfig.shouldShowNativeAd(),
            "loading_states" to loadingStates.mapValues { it.value.get() }
        )
    }
    
    // ============================================================================
    // Private Methods
    // ============================================================================
    
    private fun getCacheKey(template: NativeAdTemplate): String {
        return "native_${template.name}"
    }
    
    private fun updateState(template: NativeAdTemplate, result: NativeAdResult) {
        when (template) {
            NativeAdTemplate.INLINE_SMALL -> _inlineSmallState.value = result
            NativeAdTemplate.INLINE_MEDIUM -> _inlineMediumState.value = result
            NativeAdTemplate.LARGE_HEIGHT -> _largeHeightState.value = result
        }
    }
    
    private fun updateAllStates(result: NativeAdResult) {
        _inlineSmallState.value = result
        _inlineMediumState.value = result
        _largeHeightState.value = result
    }
    
    private fun loadNativeAd(
        template: NativeAdTemplate,
        onResult: (NativeAdResult) -> Unit
    ) {
        val adId = "native_${template.name}"
        AdsLogger.registerAd(adId, "native")
        AdsLogger.updateAdStatus(adId, AdStatus.INITIALIZING)
        AdsLogger.markNativeRequest()
        
        val nativeAdOptions = NativeAdOptions.Builder()
            .setMediaAspectRatio(
                when {
                    template.aspectRatio >= 3f -> NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE
                    template.aspectRatio >= 1.5f -> NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT
                    else -> NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_SQUARE
                }
            )
            .setRequestMultipleImages(false)
            .build()
        
        val adLoader = AdLoader.Builder(context, adUnitsProvider.nativeAdUnitId)
            .forNativeAd { nativeAd ->
                AdsLogger.updateAdStatus(adId, AdStatus.READY)
                AdsLogger.d(TAG, "[$template] Native ad loaded")
                
                // Attach ILRD listener
                nativeAd.setOnPaidEventListener { adValue: AdValue ->
                    val responseInfo = nativeAd.responseInfo
                    val adapterClassName = responseInfo?.loadedAdapterResponseInfo?.adapterClassName
                    
                    adRevenueReporter.reportNativeRevenue(
                        adUnitsProvider.nativeAdUnitId,
                        adValue.valueMicros,
                        adValue.currencyCode,
                        adValue.precisionType,
                        adapterClassName = adapterClassName,
                        template = template.name,
                        route = null
                    )
                }
                
                analyticsLogger.logEvent("ad_native_loaded", mapOf(
                    "template" to template.name,
                    "ad_unit_id" to adUnitsProvider.nativeAdUnitId
                ))
                
                onResult(NativeAdResult.Success(nativeAd))
            }
            .withNativeAdOptions(nativeAdOptions)
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    val errorMsg = "Code: ${loadAdError.code} - ${loadAdError.message}"
                    AdsLogger.updateAdStatus(adId, AdStatus.FAILED, errorMsg)
                    AdsLogger.e(TAG, "[$template] Failed to load: $errorMsg")
                    
                    analyticsLogger.logEvent("ad_native_load_failed", mapOf(
                        "template" to template.name,
                        "error_code" to loadAdError.code,
                        "error_message" to loadAdError.message,
                        "ad_unit_id" to adUnitsProvider.nativeAdUnitId
                    ))
                    
                    onResult(NativeAdResult.Error(loadAdError.message, loadAdError.code))
                }
                
                override fun onAdClicked() {
                    AdsLogger.d(TAG, "[$template] Native ad clicked")
                    
                    analyticsLogger.logEvent("ad_native_clicked", mapOf(
                        "template" to template.name,
                        "ad_unit_id" to adUnitsProvider.nativeAdUnitId
                    ))
                }
                
                override fun onAdOpened() {
                    AdsLogger.d(TAG, "[$template] Native ad opened")
                }
                
                override fun onAdClosed() {
                    AdsLogger.d(TAG, "[$template] Native ad closed")
                }
            })
            .build()
        
        adLoader.loadAd(AdRequest.Builder().build())
    }
    
    private suspend fun loadNativeAdSuspend(
        template: NativeAdTemplate
    ): NativeAdResult = suspendCancellableCoroutine { continuation ->
        loadNativeAd(template) { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
    }
}
