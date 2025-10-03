# 🔄 Banner Auto-Refresh Implementation
*Using AdMob's Built-in Auto-Refresh*

## 📋 Current Status
**Banner hiện tại CHƯA có auto-refresh enabled**.

### ✅ Có sẵn:
- Static banner loading (load 1 lần)
- BannerPreloader (preload optimization)
- Anti-flickering trong LazyColumn
- State preservation

### ❌ Cần enable:
- AdMob UI auto-refresh setting
- Banner refresh rate configuration
- Proper AdView lifecycle management

---

## 🎯 AdMob Auto-Refresh Setup

### **Cách 1: Enable qua AdMob Console (Recommended)**

1. **Truy cập AdMob Console:**
   ```
   https://apps.admob.com/
   ```

2. **Navigate to Ad Units:**
   ```
   Apps → [Your App] → Ad units → [Banner Ad Unit]
   ```

3. **Configure Refresh Rate:**
   ```
   Ad unit settings → Advanced settings → Refresh rate
   
   Options:
   - 30 seconds
   - 60 seconds  
   - 90 seconds
   - 120 seconds (2 minutes) - Recommended
   - Manual refresh only
   ```

4. **Best Practices:**
   - **Recommended**: 60-120 seconds
   - **Avoid**: Under 30 seconds (poor UX)
   - **Consider**: User behavior patterns

### **Cách 2: Programmatic Control (No Custom Timer)**

```kotlin
// In AdaptiveBanner.kt - AdMob handles refresh automatically
@Composable
fun AdaptiveBanner(
    modifier: Modifier = Modifier,
    enableAutoRefresh: Boolean = true, // Just enable/disable
    adUnitsProvider: AdUnitsProvider = hiltViewModel(),
    adsConfig: AdsConfig = hiltViewModel(),
    vipGate: VipGate = hiltViewModel(),
    analyticsLogger: AnalyticsLogger = hiltViewModel()
) {
    // ... existing code ...
    
    AndroidView(
        modifier = modifier.fillMaxWidth().height(bannerHeight),
        factory = { ctx ->
            AdsLogger.registerAd(bannerId, "banner")
            AdView(ctx).apply {
                // AdMob auto-refresh is configured in AdMob Console
                // No need for custom timers or coroutines
                
                setAdSize(adaptiveSize)
                adUnitId = adUnitsProvider.bannerAdUnitId
                
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        AdsLogger.d("Banner", "Ad loaded (auto-refresh by AdMob)")
                        analyticsLogger.logEvent("ad_banner_loaded", mapOf(
                            "auto_refresh_enabled" to enableAutoRefresh,
                            "refresh_source" to "admob_native"
                        ))
                    }
                    
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        AdsLogger.e("Banner", "Ad refresh failed: ${error.message}")
                    }
                }
                
                // Single load - AdMob handles refresh
                val adRequest = AdRequest.Builder().build()
                loadAd(adRequest)
                
                AdsLogger.d("Banner", "AdMob auto-refresh enabled: $enableAutoRefresh")
            }
        },
        update = { adView ->
            // No manual refresh logic needed
            AdsLogger.d("Banner", "AdView update (AdMob handles refresh)")
        }
    )
}
```

### **Cách 3: Enhanced AdsConfig**

```kotlin
data class AdsConfig(
    // Existing properties...
    
    // AdMob refresh settings (for analytics/logging only)
    val trackBannerRefresh: Boolean = true,
    val enableBannerRefreshAnalytics: Boolean = true
) {
    
    /**
     * Check if banner refresh tracking is enabled
     * Note: Actual refresh is handled by AdMob Console settings
     */
    fun shouldTrackBannerRefresh(): Boolean = trackBannerRefresh
}
```

---

## 🛠️ Implementation (AdMob Native Refresh)

### **1. Updated AdaptiveBanner**

```kotlin
/*
 * AdaptiveBanner with AdMob auto-refresh
 * Refresh rate configured in AdMob Console, not in code
 */
@Composable
fun AdaptiveBanner(
    modifier: Modifier = Modifier,
    trackRefresh: Boolean = true,
    adUnitsProvider: AdUnitsProvider = hiltViewModel(),
    adsConfig: AdsConfig = hiltViewModel(),
    vipGate: VipGate = hiltViewModel(),
    analyticsLogger: AnalyticsLogger = hiltViewModel()
) {
    val context = LocalContext.current
    val bannerId = "banner_bottom"
    
    // Don't show banner if VIP or disabled
    if (vipGate.isVip() || !adsConfig.shouldShowBanner()) {
        DisposableEffect(Unit) {
            AdsLogger.registerAd(bannerId, "banner")
            AdsLogger.updateAdStatus(bannerId, AdStatus.NOT_AVAILABLE,
                if (vipGate.isVip()) "VIP user" else "Banner disabled")
            onDispose { AdsLogger.unregisterAd(bannerId) }
        }
        return
    }
    
    var bannerHeight by remember { mutableStateOf(60.dp) }
    var refreshCount by remember { mutableStateOf(0) }
    
    AndroidView(
        modifier = modifier.fillMaxWidth().height(bannerHeight),
        factory = { ctx ->
            AdsLogger.registerAd(bannerId, "banner")
            AdsLogger.updateAdStatus(bannerId, AdStatus.INITIALIZING)
            
            AdView(ctx).apply {
                val displayMetrics = ctx.resources.displayMetrics
                val displayDensity = displayMetrics.density
                val adWidthPixels = displayMetrics.widthPixels
                val adWidthDp = (adWidthPixels / displayDensity).toInt()
                
                val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    ctx, adWidthDp
                )
                
                setAdSize(adaptiveSize)
                adUnitId = adUnitsProvider.bannerAdUnitId
                
                // Calculate banner height
                val heightInPixels = adaptiveSize.getHeightInPixels(ctx)
                val heightInDp = (heightInPixels / displayDensity)
                bannerHeight = heightInDp.dp
                
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        refreshCount++
                        AdsLogger.updateAdStatus(bannerId, AdStatus.READY)
                        AdsLogger.d("Banner", "Banner loaded (refresh #$refreshCount)")
                        
                        if (trackRefresh && adsConfig.shouldTrackBannerRefresh()) {
                            analyticsLogger.logEvent("ad_banner_loaded", mapOf(
                                "ad_unit_id" to adUnitsProvider.bannerAdUnitId,
                                "refresh_count" to refreshCount,
                                "refresh_type" to "admob_native",
                                "is_auto_refresh" to true
                            ))
                        }
                    }
                    
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        val errorMsg = "Code: ${loadAdError.code} - ${loadAdError.message}"
                        AdsLogger.updateAdStatus(bannerId, AdStatus.FAILED, errorMsg)
                        AdsLogger.e("Banner", "Banner refresh failed: $errorMsg")
                        
                        if (trackRefresh) {
                            analyticsLogger.logEvent("ad_banner_refresh_failed", mapOf(
                                "error_code" to loadAdError.code,
                                "error_message" to loadAdError.message,
                                "refresh_count" to refreshCount
                            ))
                        }
                    }
                    
                    override fun onAdImpression() {
                        AdsLogger.updateAdStatus(bannerId, AdStatus.SHOWING)
                        AdsLogger.d("Banner", "Banner impression (refresh #$refreshCount)")
                        
                        if (trackRefresh) {
                            analyticsLogger.logEvent("ad_banner_impression", mapOf(
                                "refresh_count" to refreshCount,
                                "is_refreshed_ad" to (refreshCount > 1)
                            ))
                        }
                    }
                    
                    override fun onAdClicked() {
                        AdsLogger.d("Banner", "Banner clicked (refresh #$refreshCount)")
                        
                        analyticsLogger.logEvent("ad_banner_clicked", mapOf(
                            "refresh_count" to refreshCount,
                            "ad_unit_id" to adUnitsProvider.bannerAdUnitId
                        ))
                    }
                }
                
                // Load ad once - AdMob handles auto-refresh
                val adRequest = AdRequest.Builder().build()
                loadAd(adRequest)
                
                AdsLogger.d("Banner", "Banner initialized - AdMob will handle auto-refresh")
            }
        },
        update = { adView ->
            // AdMob handles refresh automatically
            // No manual intervention needed
            AdsLogger.d("Banner", "AdView update - AdMob native refresh active")
        }
    )
    
    DisposableEffect(Unit) {
        onDispose {
            AdsLogger.d("Banner", "Banner disposed (total refreshes: $refreshCount)")
            AdsLogger.unregisterAd(bannerId)
        }
    }
}
```

### **2. BannerAdItem with AdMob Refresh**

```kotlin
@Composable
fun BannerAdItem(
    adId: String = "banner_ad",
    trackRefresh: Boolean = true,
    modifier: Modifier = Modifier,
    showBackground: Boolean = false,
    topPadding: Dp = 8.dp,
    bottomPadding: Dp = 8.dp,
    adUnitsProvider: AdUnitsProvider = hiltViewModel(),
    adsConfig: AdsConfig = hiltViewModel(),
    vipGate: VipGate = hiltViewModel(),
    analyticsLogger: AnalyticsLogger = hiltViewModel()
) {
    // Don't show banner if VIP or disabled
    if (vipGate.isVip() || !adsConfig.shouldShowBanner()) {
        DisposableEffect(Unit) {
            AdsLogger.registerAd(adId, "banner")
            AdsLogger.updateAdStatus(adId, AdStatus.NOT_AVAILABLE,
                if (vipGate.isVip()) "VIP user" else "Banner disabled")
            onDispose { AdsLogger.unregisterAd(adId) }
        }
        return
    }
    
    var bannerHeight by remember(adId) { mutableStateOf(60.dp) }
    var refreshCount by remember(adId) { mutableStateOf(0) }
    
    val containerModifier = if (showBackground) {
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(top = topPadding, bottom = bottomPadding)
    } else {
        modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = bottomPadding)
    }
    
    Box(modifier = containerModifier) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(bannerHeight),
            factory = { ctx ->
                AdsLogger.registerAd(adId, "banner")
                AdsLogger.updateAdStatus(adId, AdStatus.INITIALIZING)
                
                AdView(ctx).apply {
                    val displayMetrics = ctx.resources.displayMetrics
                    val displayDensity = displayMetrics.density
                    val adWidthPixels = displayMetrics.widthPixels
                    val adWidthDp = (adWidthPixels / displayDensity).toInt()
                    
                    val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        ctx, adWidthDp
                    )
                    
                    setAdSize(adaptiveSize)
                    adUnitId = adUnitsProvider.bannerAdUnitId
                    
                    // Calculate banner height
                    val heightInPixels = adaptiveSize.getHeightInPixels(ctx)
                    val heightInDp = (heightInPixels / displayDensity)
                    bannerHeight = heightInDp.dp
                    
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            refreshCount++
                            AdsLogger.updateAdStatus(adId, AdStatus.READY)
                            AdsLogger.d("BannerAdItem", "[$adId] Loaded (refresh #$refreshCount)")
                            
                            if (trackRefresh && adsConfig.shouldTrackBannerRefresh()) {
                                analyticsLogger.logEvent("ad_banner_item_loaded", mapOf(
                                    "ad_id" to adId,
                                    "refresh_count" to refreshCount,
                                    "is_auto_refresh" to (refreshCount > 1)
                                ))
                            }
                        }
                        
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            val errorMsg = "Code: ${loadAdError.code} - ${loadAdError.message}"
                            AdsLogger.updateAdStatus(adId, AdStatus.FAILED, errorMsg)
                            AdsLogger.e("BannerAdItem", "[$adId] Refresh failed: $errorMsg")
                        }
                        
                        override fun onAdImpression() {
                            AdsLogger.updateAdStatus(adId, AdStatus.SHOWING)
                            AdsLogger.d("BannerAdItem", "[$adId] Impression (refresh #$refreshCount)")
                        }
                        
                        override fun onAdClicked() {
                            AdsLogger.d("BannerAdItem", "[$adId] Clicked")
                            analyticsLogger.logEvent("ad_banner_item_clicked", mapOf(
                                "ad_id" to adId,
                                "refresh_count" to refreshCount
                            ))
                        }
                    }
                    
                    // Load ad - AdMob will handle refresh automatically
                    val adRequest = AdRequest.Builder().build()
                    loadAd(adRequest)
                    
                    AdsLogger.d("BannerAdItem", "[$adId] AdMob auto-refresh enabled")
                }
            },
            update = { adView ->
                // AdMob handles refresh - no manual intervention
                AdsLogger.d("BannerAdItem", "[$adId] Update (AdMob native refresh)")
            }
        )
    }
    
    DisposableEffect(adId) {
        onDispose {
            AdsLogger.d("BannerAdItem", "[$adId] Disposed (total refreshes: $refreshCount)")
            AdsLogger.unregisterAd(adId)
        }
    }
}
```

---

## 📊 AdMob Console Configuration

### **Step-by-Step Setup:**

1. **Login to AdMob:**
   ```
   https://apps.admob.com/
   ```

2. **Select Your App:**
   ```
   Apps → [BaseAds Sample App] → Ad units
   ```

3. **Edit Banner Ad Unit:**
   ```
   [Banner Ad Unit] → Settings → Advanced settings
   ```

4. **Configure Refresh Rate:**
   ```
   Refresh rate: 60 seconds (Recommended)
   
   Available options:
   • 30 seconds - Fast refresh (may hurt UX)
   • 60 seconds - Balanced (Recommended)
   • 90 seconds - Conservative
   • 120 seconds - Slow but safe
   • Manual only - No auto-refresh
   ```

5. **Save Settings:**
   ```
   Save → Changes will take effect within 1 hour
   ```

---

## 🎯 Benefits of AdMob Native Refresh

### ✅ **Advantages:**

1. **No Custom Code:**
   - No timers, coroutines, or complex logic
   - AdMob handles everything automatically
   - Less code = fewer bugs

2. **Optimized by Google:**
   - Smart refresh timing
   - Respects user engagement
   - Better ad fill rates

3. **Lifecycle Aware:**
   - Pauses when app backgrounded
   - Resumes when app foregrounded
   - Handles orientation changes

4. **Better Performance:**
   - No additional background threads
   - Optimized memory usage
   - Native implementation

5. **Industry Standard:**
   - Google's recommended approach
   - Used by major apps
   - Proven effectiveness

### ⚠️ **Considerations:**

1. **Less Control:**
   - Can't customize refresh logic
   - Fixed refresh intervals
   - Can't pause/resume programmatically

2. **Console Dependency:**
   - Changes require AdMob Console access
   - Takes time to propagate (up to 1 hour)
   - A/B testing requires manual changes

---

## 📈 Analytics & Monitoring

### **Tracking Refresh Performance:**

```kotlin
// In AdListener callbacks
override fun onAdLoaded() {
    analyticsLogger.logEvent("banner_auto_refresh", mapOf(
        "refresh_count" to refreshCount,
        "refresh_source" to "admob_native",
        "ad_unit_id" to adUnitId,
        "timestamp" to System.currentTimeMillis()
    ))
}

override fun onAdFailedToLoad(error: LoadAdError) {
    analyticsLogger.logEvent("banner_refresh_failed", mapOf(
        "error_code" to error.code,
        "error_message" to error.message,
        "refresh_count" to refreshCount,
        "refresh_source" to "admob_native"
    ))
}
```

### **Key Metrics to Track:**

- Refresh success rate
- Revenue per refresh
- User engagement after refresh
- Optimal refresh intervals
- Error rates by refresh count

---

## � Implementation Summary

**Current Setup:**
```kotlin
// ✅ Simple - AdMob handles everything
AdaptiveBanner(trackRefresh = true)

// ✅ In LazyColumn
BannerAdItem(
    adId = "banner_$index",
    trackRefresh = true
)
```

**AdMob Console:**
```
Refresh Rate: 60 seconds
Status: Enabled
Propagation: ~1 hour
```

**No Custom Logic Needed:**
- ❌ No timers
- ❌ No coroutines  
- ❌ No lifecycle management
- ✅ Just load once, AdMob does the rest!