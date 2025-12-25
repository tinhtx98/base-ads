# 📖 Base-Ads Integration Guide

> **Version:** 2.0.0  
> **Date:** 25/12/2024  
> **Format:** AAR Library  
> **Mediation:** Bidding (Real-time auction)

---

## 📋 Mục Lục

1. [Giới Thiệu](#1-giới-thiệu)
2. [Yêu Cầu Hệ Thống](#2-yêu-cầu-hệ-thống)
3. [Cài Đặt AAR](#3-cài-đặt-aar)
4. [Cấu Hình AndroidManifest](#4-cấu-hình-androidmanifest)
5. [Cấu Hình Hilt DI](#5-cấu-hình-hilt-di)
6. [Khởi Tạo SDK](#6-khởi-tạo-sdk)
7. [Banner Ads](#7-banner-ads)
8. [Interstitial Ads](#8-interstitial-ads)
9. [Native Ads](#9-native-ads)
10. [Open App Ads](#10-open-app-ads)
11. [VIP Gate Integration](#11-vip-gate-integration)
12. [Analytics & Revenue Tracking](#12-analytics--revenue-tracking)
13. [ProGuard Configuration](#13-proguard-configuration)
14. [Troubleshooting](#14-troubleshooting)
15. [Best Practices](#15-best-practices)

---

## 1. Giới Thiệu

Base-ads là một thư viện Android được đóng gói dưới dạng AAR, cung cấp:

- **Banner Ads**: Adaptive banner với auto-refresh tracking
- **Interstitial Ads**: Smart show policy với cooldown và daily cap
- **Native Ads**: 3 templates (Inline Small, Medium, Large Height)
- **Open App Ads**: Tự động show khi app từ background lên foreground
- **Mediation Bidding**: Real-time auction với Vungle, IronSource, Meta, InMobi
- **ILRD Tracking**: Impression Level Revenue Data cho tất cả ad formats
- **VIP Gate**: Tự động disable ads cho premium users

---

## 2. Yêu Cầu Hệ Thống

```groovy
// Minimum requirements
minSdk = 26
compileSdk = 35
targetSdk = 35
jvmTarget = "17"

// Required plugins
- com.google.dagger.hilt.android
- com.google.devtools.ksp
- com.google.gms.google-services
```

### Dependencies tự động đi kèm AAR:

| Library | Version | Purpose |
|---------|---------|---------|
| play-services-ads | 24.7.0 | Google AdMob SDK |
| vungle mediation | 7.6.0.0 | Vungle Liftoff bidding |
| ironsource mediation | 9.1.0.0 | IronSource bidding |
| facebook mediation | 6.21.0.0 | Meta bidding |
| inmobi mediation | 10.8.8.1 | InMobi bidding |
| firebase-analytics | Latest | Analytics tracking |

---

## 3. Cài Đặt AAR

### Step 1: Copy AAR file

```
your-app/
├── app/
│   ├── libs/
│   │   └── base-ads-release.aar    // ← Copy vào đây
│   └── build.gradle.kts
```

### Step 2: Cấu hình build.gradle.kts (app)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    // ... existing config
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Base Ads AAR
    implementation(files("libs/base-ads-release.aar"))
    
    // Required dependencies (must add manually)
    // Google Mobile Ads SDK
    implementation("com.google.android.gms:play-services-ads:24.7.0")
    
    // Mediation adapters (bidding)
    implementation("com.google.ads.mediation:vungle:7.6.0.0")
    implementation("com.google.ads.mediation:ironsource:9.1.0.0")
    implementation("com.google.ads.mediation:facebook:6.21.0.0")
    implementation("com.google.ads.mediation:inmobi:10.8.8.1")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.56.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("com.google.dagger:hilt-compiler:2.56.1")
    
    // Lifecycle (for Open App Ads)
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
}
```

### Step 3: Cấu hình settings.gradle.kts

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        
        // IronSource Maven repo
        maven { url = uri("https://android-sdk.is.com/") }
        
        // Vungle Maven repo (nếu cần)
        maven { url = uri("https://sdk.vungle.com/") }
    }
}
```

---

## 4. Cấu Hình AndroidManifest

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Required permissions (đã có trong AAR) -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="com.google.android.gms.permission.AD_ID" />
    
    <application
        android:name=".MyApplication"
        ...>
        
        <!-- ⚠️ REQUIRED: AdMob App ID -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY" />
        
        <!-- Optional: Delay app measurement (for consent) -->
        <meta-data
            android:name="com.google.android.gms.ads.DELAY_APP_MEASUREMENT_INIT"
            android:value="true" />
            
        <!-- Activities -->
        <activity
            android:name=".MainActivity"
            android:exported="true">
            ...
        </activity>
        
    </application>
</manifest>
```

---

## 5. Cấu Hình Hilt DI

### Step 1: Tạo Hilt Module trong app

```kotlin
// file: di/AppAdsModule.kt
package com.yourapp.di

import com.tinhtx.baseads.core.AdUnitsProvider
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdsConstants
import com.tinhtx.baseads.core.VipGate
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppAdsModule {
    
    /**
     * Provides Ad Unit IDs
     * ⚠️ Replace với production IDs của bạn
     */
    @Provides
    @Singleton
    fun provideAdUnitsProvider(): AdUnitsProvider {
        return object : AdUnitsProvider {
            // Banner Ad Unit
            override val bannerAdUnitId: String
                get() = "ca-app-pub-XXXXX/banner_id"  // ← Replace
            
            // Interstitial Ad Unit
            override val interstitialAdUnitId: String
                get() = "ca-app-pub-XXXXX/interstitial_id"  // ← Replace
            
            // Native Ad Unit
            override val nativeAdUnitId: String
                get() = "ca-app-pub-XXXXX/native_id"  // ← Replace
            
            // Open App Ad Unit
            override val openAppAdUnitId: String
                get() = "ca-app-pub-XXXXX/open_app_id"  // ← Replace
        }
    }
    
    /**
     * Provides Ads Configuration
     * Customize theo nhu cầu của app
     */
    @Provides
    @Singleton
    fun provideAdsConfig(): AdsConfig {
        return AdsConfig(
            // Master toggles
            enableAds = true,
            enableInterstitial = true,
            enableBanner = true,
            enableNativeAd = true,
            enableOpenAppAd = true,
            
            // Route blocklist (không show interstitial ở các screen này)
            interstitialBlocklistRoutes = setOf(
                "premium",
                "vip",
                "subscription",
                "payment",
                "checkout",
                "auth",
                "login"
            ),
            
            // Open App Ad excluded activities
            openAppExcludedActivities = setOf(
                "SplashActivity",
                "PaymentActivity"
            ),
            
            // Bidding config
            enableBiddingOptimization = true,
            enableMediationAnalytics = true,
            enableVungleBidding = true,
            enableIronSourceBidding = true,
            enableMetaBidding = true
        )
    }
    
    /**
     * Provides VIP Gate Implementation
     * Connect với hệ thống subscription của bạn
     */
    @Provides
    @Singleton
    fun provideVipGate(
        // Inject your subscription/premium manager here
        // subscriptionManager: SubscriptionManager
    ): VipGate {
        return object : VipGate {
            override fun isVip(): Boolean {
                // Return true nếu user đã mua premium
                // return subscriptionManager.isPremium()
                return false  // Default: không phải VIP
            }
        }
    }
}
```

### Step 2: Setup Application class

```kotlin
// file: MyApplication.kt
package com.yourapp

import android.app.Application
import com.tinhtx.baseads.core.AdsInitializer
import com.tinhtx.baseads.interstitial.InterstitialAdManager
import com.tinhtx.baseads.nativead.NativeAdManager
import com.tinhtx.baseads.openapp.OpenAppAdManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject lateinit var adsInitializer: AdsInitializer
    @Inject lateinit var interstitialAdManager: InterstitialAdManager
    @Inject lateinit var nativeAdManager: NativeAdManager
    @Inject lateinit var openAppAdManager: OpenAppAdManager
    
    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize Ads SDK
        adsInitializer.initialize(
            context = this,
            testDeviceIds = listOf(
                // Add your test device IDs for development
                // "YOUR_TEST_DEVICE_ID_1",
                // "YOUR_TEST_DEVICE_ID_2"
            )
        )
        
        // 2. Preload Interstitial Ads
        interstitialAdManager.forcePreload()
        
        // 3. Preload Native Ads
        nativeAdManager.preloadAll()
        
        // 4. Initialize Open App Ads
        openAppAdManager.initialize(
            application = this,
            excludeActivities = listOf(
                "SplashActivity",
                "PaymentActivity"
            )
        )
    }
}
```

---

## 6. Khởi Tạo SDK

### Test Mode vs Production Mode

```kotlin
// Test Mode (Development)
adsInitializer.initialize(
    context = this,
    testDeviceIds = listOf("YOUR_TEST_DEVICE_ID")
)

// Production Mode
adsInitializer.initializeProductionMode(context = this)
```

### Kiểm tra trạng thái

```kotlin
// Check if initialized
if (adsInitializer.isInitialized()) {
    // Safe to show ads
}

// Wait for initialization
adsInitializer.onInitialized {
    // SDK ready, can show ads
}
```

---

## 7. Banner Ads

### Compose Implementation

```kotlin
import com.tinhtx.baseads.banner.AdaptiveBanner
import com.tinhtx.baseads.banner.BannerPreloader

@Composable
fun HomeScreen(
    adUnitsProvider: AdUnitsProvider,
    adsConfig: AdsConfig,
    vipGate: VipGate,
    analyticsLogger: AnalyticsLogger,
    bannerPreloader: BannerPreloader
) {
    Scaffold(
        bottomBar = {
            // Banner at bottom
            AdaptiveBanner(
                adUnitsProvider = adUnitsProvider,
                adsConfig = adsConfig,
                vipGate = vipGate,
                analyticsLogger = analyticsLogger,
                bannerPreloader = bannerPreloader,
                trackRefresh = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { padding ->
        // Screen content
    }
}
```

### Banner Preloading (Optional)

```kotlin
// In MainActivity.onCreate() or Application.onCreate()
@Inject lateinit var bannerPreloader: BannerPreloader

bannerPreloader.preloadBanner(context)
```

---

## 8. Interstitial Ads

### Basic Usage

```kotlin
@Inject lateinit var interstitialAdManager: InterstitialAdManager

// Option 1: Smart show (respects policy)
lifecycleScope.launch {
    val shown = interstitialAdManager.maybeShow(
        activity = this@MainActivity,
        currentRoute = "home"
    )
    if (shown) {
        // Ad was shown
    }
}

// Option 2: Force show (bypass policy)
interstitialAdManager.show(
    activity = this@MainActivity,
    onShown = { 
        // Ad dismissed, proceed
    },
    onNoAdAvailable = {
        // No ad, proceed immediately
    },
    onShowFailed = { error ->
        // Failed, proceed
    },
    waitTimeoutMs = 2000  // Wait up to 2s for ad to load
)
```

### Smart Navigation Integration

```kotlin
import com.tinhtx.baseads.ext.navigateSmart
import com.tinhtx.baseads.ext.MarkScreenOpened

@Composable
fun MyScreen(
    navController: NavController,
    interstitialAdManager: InterstitialAdManager,
    analyticsLogger: AnalyticsLogger,
    adsConfig: AdsConfig
) {
    val activity = LocalContext.current as Activity
    
    // Mark screen opened (for timing policy)
    MarkScreenOpened(
        ads = interstitialAdManager,
        analytics = analyticsLogger,
        screenName = "my_screen"
    )
    
    Button(onClick = {
        lifecycleScope.launch {
            // Navigate with smart ad showing
            navController.navigateSmart(
                route = "detail_screen",
                ads = interstitialAdManager,
                analytics = analyticsLogger,
                config = adsConfig,
                activity = activity
            )
        }
    }) {
        Text("Go to Detail")
    }
}
```

### Policy Configuration

Default policy:
- **Cooldown**: 15 seconds between shows
- **Daily Cap**: 15 interstitials per day
- **First Launch Delay**: 20 seconds after app start
- **Screen Open Delay**: 5 seconds after screen opens

---

## 9. Native Ads

### Template Types

| Template | Height | Use Case | eCPM |
|----------|--------|----------|------|
| `INLINE_SMALL` | ~100dp | Feed items | $2-6 |
| `INLINE_MEDIUM` | ~150dp | Feed with media | $4-8 |
| `LARGE_HEIGHT` | ~280dp | Article end | $8-15 |

### Compose Implementation

```kotlin
import com.tinhtx.baseads.nativead.NativeAdInline
import com.tinhtx.baseads.nativead.NativeAdMedium
import com.tinhtx.baseads.nativead.NativeAdLargeHeight

@Composable
fun FeedScreen(
    nativeAdManager: NativeAdManager
) {
    LazyColumn {
        items(feedItems) { item ->
            FeedItemCard(item)
        }
        
        // Insert inline native ad every 5 items
        item {
            NativeAdInline(
                nativeAdManager = nativeAdManager,
                position = 5,
                route = "feed",
                onAdLoaded = { /* tracking */ },
                onAdFailed = { error -> /* handle */ }
            )
        }
        
        // Insert medium native ad
        item {
            NativeAdMedium(
                nativeAdManager = nativeAdManager,
                position = 10,
                route = "feed"
            )
        }
    }
}

// Article end
@Composable
fun ArticleScreen(
    nativeAdManager: NativeAdManager
) {
    Column {
        // Article content...
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Large native ad at end
        NativeAdLargeHeight(
            nativeAdManager = nativeAdManager,
            position = 0,
            route = "article",
            showShimmer = true
        )
    }
}
```

### Manual Loading (Advanced)

```kotlin
// Get ad with suspend function
lifecycleScope.launch {
    when (val result = nativeAdManager.getAd(NativeAdTemplate.INLINE_MEDIUM)) {
        is NativeAdResult.Success -> {
            val nativeAd = result.ad
            // Use ad...
        }
        is NativeAdResult.Error -> {
            Log.e("NativeAd", "Failed: ${result.message}")
        }
        is NativeAdResult.VipUser -> {
            // User is VIP, don't show ads
        }
        is NativeAdResult.Disabled -> {
            // Native ads disabled
        }
        else -> {}
    }
}

// Get cached ad only (no network call)
val cachedAd = nativeAdManager.getCachedAd(NativeAdTemplate.LARGE_HEIGHT)
```

---

## 10. Open App Ads

### Automatic Mode (Recommended)

Open App Ads tự động show khi:
1. App từ background lên foreground
2. Đã qua cooldown (30 seconds mặc định)
3. Activity hiện tại không bị excluded
4. User không phải VIP

```kotlin
// In Application.onCreate()
openAppAdManager.initialize(
    application = this,
    excludeActivities = listOf(
        "SplashActivity",
        "PaymentActivity",
        "PremiumActivity"
    )
)
```

### Manual Show (Optional)

```kotlin
// Force show (for specific flows like level complete)
val result = openAppAdManager.showIfAvailable(
    activity = this,
    bypassCooldown = true  // Ignore cooldown
)

when (result) {
    OpenAppAdResult.Shown -> { /* Ad shown */ }
    OpenAppAdResult.NoAdAvailable -> { /* No ad ready */ }
    OpenAppAdResult.CooldownActive -> { /* Still in cooldown */ }
    OpenAppAdResult.VipUser -> { /* VIP user */ }
    else -> {}
}
```

### Exclude Additional Activities

```kotlin
// Add runtime exclusion
openAppAdManager.excludeActivity("OnboardingActivity")

// Remove exclusion
openAppAdManager.includeActivity("OnboardingActivity")
```

### On Dismiss Callback

```kotlin
openAppAdManager.setOnAdDismissedCallback {
    // Ad dismissed, app is now visible
    // Good place to refresh content
}
```

---

## 11. VIP Gate Integration

### Basic Implementation

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppAdsModule {
    
    @Provides
    @Singleton
    fun provideVipGate(
        subscriptionRepository: SubscriptionRepository
    ): VipGate {
        return object : VipGate {
            override fun isVip(): Boolean {
                return subscriptionRepository.isPremiumUser()
            }
        }
    }
}
```

### With StateFlow (Reactive)

```kotlin
class AppVipGate @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : VipGate {
    
    override fun isVip(): Boolean {
        return subscriptionRepository.isPremiumFlow.value
    }
}

// Usage in ViewModel
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vipGate: VipGate
) : ViewModel() {
    
    val showAds = !vipGate.isVip()
}
```

---

## 12. Analytics & Revenue Tracking

### Tự động tracking events:

| Event | Description |
|-------|-------------|
| `ad_banner_loaded` | Banner loaded successfully |
| `ad_banner_impression` | Banner impression |
| `ad_banner_paid` | Banner revenue (ILRD) |
| `ad_interstitial_loaded` | Interstitial loaded |
| `ad_interstitial_impression` | Interstitial impression |
| `ad_interstitial_paid` | Interstitial revenue (ILRD) |
| `ad_native_loaded` | Native ad loaded |
| `ad_native_impression` | Native ad impression |
| `ad_native_paid` | Native ad revenue (ILRD) |
| `ad_open_app_loaded` | Open app ad loaded |
| `ad_open_app_impression` | Open app ad impression |
| `ad_open_app_paid` | Open app ad revenue (ILRD) |

### Custom Analytics Logger (Optional)

```kotlin
// Replace Firebase with custom implementation
class MyAnalyticsLogger : AnalyticsLogger {
    
    override fun logEvent(name: String, params: Map<String, Any?>) {
        // Send to your analytics service
        MyAnalytics.track(name, params)
    }
    
    override fun setCurrentScreen(screen: String) {
        MyAnalytics.setScreen(screen)
    }
}

// Provide in Hilt Module
@Provides
@Singleton
fun provideAnalyticsLogger(): AnalyticsLogger = MyAnalyticsLogger()
```

---

## 13. ProGuard Configuration

AAR đã bao gồm consumer-rules.pro. Nếu gặp vấn đề, thêm vào `proguard-rules.pro` của app:

```proguard
# Base Ads
-keep class com.tinhtx.baseads.** { *; }

# Google Ads SDK
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.mediation.** { *; }

# Native Ads
-keep class com.google.android.gms.ads.nativead.** { *; }

# Open App Ads
-keep class com.google.android.gms.ads.appopen.** { *; }
-keep class androidx.lifecycle.ProcessLifecycleOwner { *; }

# Mediation Adapters
-keep class com.vungle.** { *; }
-keep class com.ironsource.** { *; }
-keep class com.facebook.ads.** { *; }
-keep class com.inmobi.** { *; }

# ILRD Listeners
-keep class * implements com.google.android.gms.ads.OnPaidEventListener { *; }
```

---

## 14. Troubleshooting

### Common Issues

#### 1. "No fill" cho tất cả ads

**Nguyên nhân:**
- Đang dùng test ad units với production app
- AdMob account chưa được approved
- Mediation adapters chưa được configure đúng

**Giải pháp:**
```kotlin
// Development: Use test IDs
override val interstitialAdUnitId: String
    get() = AdsConstants.TestAdUnits.INTERSTITIAL

// Production: Use real IDs
override val interstitialAdUnitId: String
    get() = "ca-app-pub-XXXXX/YYYYY"
```

#### 2. Open App Ad không show

**Checklist:**
- [ ] Đã gọi `openAppAdManager.initialize(application)` trong Application.onCreate()
- [ ] Activity hiện tại không trong excluded list
- [ ] Đã qua cooldown (30s)
- [ ] User không phải VIP
- [ ] Daily cap chưa đạt (20/day)

#### 3. Native Ad không hiện

**Checklist:**
- [ ] Đã preload: `nativeAdManager.preloadAll()`
- [ ] Template đúng: `NativeAdTemplate.INLINE_SMALL` / `LARGE_HEIGHT`
- [ ] Ad unit ID đúng type (Native, không phải Banner)

#### 4. Crash khi minify enabled

Thêm vào proguard-rules.pro:
```proguard
-keep class * implements com.google.android.gms.ads.mediation.MediationAdapter { *; }
-keep class * implements com.google.android.gms.ads.mediation.rtb.RtbAdapter { *; }
```

### Debug Tools

```kotlin
// Log all ads status
AdsLogger.logAllAdsStatus()

// Get ads summary
val summary = AdsLogger.getAdsSummary()

// Get specific manager debug info
val interstitialInfo = interstitialAdManager.getDebugInfo()
val nativeInfo = nativeAdManager.getDebugInfo()
val openAppInfo = openAppAdManager.getDebugInfo()

// Log rate summary
AdsLogger.logRateSummary()
```

---

## 15. Best Practices

### Performance

1. **Preload early**: Gọi preload trong Application.onCreate()
2. **Cache wisely**: Native ads được cache tự động (max 5)
3. **Destroy unused ads**: Compose components tự cleanup trong DisposableEffect

### Revenue Optimization

1. **Use all ad formats**: Banner + Interstitial + Native + Open App = Maximum revenue
2. **Strategic placement**: 
   - Native Large ở cuối article
   - Native Medium trong feed (mỗi 5-8 items)
   - Open App khi từ background lên
3. **Don't over-show**: Respect cooldowns và daily caps

### User Experience

1. **Loading states**: Sử dụng shimmer placeholders
2. **Graceful degradation**: Handle "no fill" scenarios
3. **VIP experience**: Disable all ads cho premium users
4. **Route blocking**: Không show interstitial ở payment screens

### Testing Checklist

- [ ] Test với test ad unit IDs
- [ ] Verify tất cả adapters show "READY" trong logs
- [ ] Test VIP toggle disable/enable ads
- [ ] Test app background → foreground cho Open App
- [ ] Verify ILRD events trong Firebase Analytics
- [ ] Test release build với ProGuard enabled

---

## 📞 Support

Nếu gặp vấn đề, kiểm tra:
1. Logcat filter: `BaseAds`
2. Firebase Analytics events
3. AdMob Console → Ad units → Test

---

*Document Version: 2.0.0*  
*Last Updated: 25/12/2024*
