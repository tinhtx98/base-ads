# 📊 Base-Ads Module Investigation Summary

> **Ngày phân tích:** 25/12/2024  
> **Module:** base-ads  
> **Mục đích:** Tích hợp Native Ad, Open App Ad và kết hợp SmartInterstitial + VIP logic

---

## 📋 Mục Lục

1. [Tình Trạng Hiện Tại](#1-tình-trạng-hiện-tại)
2. [Phân Tích Mediation Bidding](#2-phân-tích-mediation-bidding)
3. [Giải Pháp Tích Hợp Native Ad](#3-giải-pháp-tích-hợp-native-ad)
4. [Giải Pháp Tích Hợp Open App Ad](#4-giải-pháp-tích-hợp-open-app-ad)
5. [Kết Hợp SmartInterstitial + VIP Logic](#5-kết-hợp-smartinterstitial--vip-logic)
6. [ProGuard & Production Readiness](#6-proguard--production-readiness)
7. [Options Ưu Tiên Doanh Thu](#7-options-ưu-tiên-doanh-thu)
8. [Implementation Roadmap](#8-implementation-roadmap)

---

## 1. Tình Trạng Hiện Tại

### ✅ Đã Có
| Component | Status | File |
|-----------|--------|------|
| **Banner Ad** | ✅ Hoàn chỉnh | `AdaptiveBanner.kt`, `BannerPreloader.kt` |
| **Interstitial Ad** | ✅ Hoàn chỉnh | `InterstitialAdManager.kt` |
| **VIP Gate** | ✅ Interface + Default | `VipGate.kt` |
| **Ad Revenue Reporting (ILRD)** | ✅ Hoàn chỉnh | `AdRevenueReporter.kt` |
| **Analytics Logger** | ✅ Firebase | `AnalyticsLogger.kt` |
| **Mediation Partners** | ✅ Bidding | Vungle, IronSource, Meta, InMobi |
| **Smart Navigation** | ✅ Extensions | `NavControllerExt.kt`, `ClickableExt.kt` |

### ❌ Chưa Có
| Component | Status | Priority |
|-----------|--------|----------|
| **Native Ad** | ❌ Folder trống | 🔴 Cao |
| **Open App Ad** | ❌ Folder trống | 🔴 Cao |
| **Rewarded Ad** | ❌ Chưa có | 🟡 Trung bình |
| **Rewarded Interstitial** | ❌ Chưa có | 🟢 Thấp |

---

## 2. Phân Tích Mediation Bidding

### ✅ KẾT LUẬN: Base-ads ĐÃ ĐÚNG với Mediation Bidding

**Bằng chứng:**

```kotlin
// build.gradle.kts - Line 28-29
buildConfigField("String", "MEDIATION_TYPE", "\"bidding\"")

// AdsInitializer.kt - Line 67-68
// Note: With bidding mediation, no manual SDK initialization needed
// All mediation is handled automatically by Google Mobile Ads SDK
```

**Mediation Adapters (Bidding):**
| Partner | Version | Type |
|---------|---------|------|
| Google AdMob | 24.7.0 | Primary |
| Vungle/Liftoff | 7.6.0.0 | Bidding |
| IronSource | 9.1.0.0 | Bidding |
| Meta (Facebook) | 6.21.0.0 | Bidding |
| InMobi | 10.8.8.1 | Bidding |

**Đặc điểm Bidding hiện tại:**
- ✅ **Không cần App Keys** - Tất cả được config qua AdMob Console
- ✅ **Real-time bidding** - Các mạng đấu giá trực tiếp
- ✅ **ILRD tracking** - Ghi nhận revenue theo impression
- ✅ **Automatic SDK init** - Google SDK tự quản lý adapters

### ⚠️ Lưu Ý Quan Trọng
- Waterfall KHÔNG được sử dụng
- Tất cả mediation partners đều tham gia real-time auction
- Revenue optimization tự động bởi AdMob

---

## 3. Giải Pháp Tích Hợp Native Ad

### 3.1 Architecture Đề Xuất

```
nativead/
├── NativeAdManager.kt          // Manager chính, singleton
├── NativeAdLoader.kt           // Load và cache logic
├── NativeAdConfig.kt           // Config cho native ad
├── NativeAdTemplate.kt         // Enum cho template types
├── compose/
│   ├── NativeAdInline.kt       // Small inline (feed item)
│   ├── NativeAdLargeHeight.kt  // Large height (article end)
│   └── NativeAdContainer.kt    // Wrapper với shimmer
└── viewbinding/
    ├── NativeAdInlineView.kt   // XML-based inline
    └── NativeAdLargeView.kt    // XML-based large
```

### 3.2 Hai Loại Native Ad

#### **Option A: Template-based (Đơn giản, nhanh triển khai)**

| Type | Kích thước | Use Case | eCPM ước tính |
|------|-----------|----------|---------------|
| **Inline (Small)** | ~100dp height | Feed items, list between | $2-5 |
| **Large Height** | ~280dp height | Article end, standalone | $5-12 |

```kotlin
enum class NativeAdTemplate {
    INLINE_SMALL,    // Tỷ lệ 4:1 hoặc 6:1
    LARGE_HEIGHT     // Tỷ lệ 1.2:1 hoặc 16:9
}
```

#### **Option B: Custom Templates (Cao cấp, revenue cao hơn)**

```kotlin
sealed class NativeAdType {
    data class Inline(
        val showMediaView: Boolean = false,  // Tăng ~30% eCPM nếu true
        val showStarRating: Boolean = true,
        val showStore: Boolean = true
    ) : NativeAdType()
    
    data class LargeHeight(
        val aspectRatio: Float = 1.91f,      // 1.91:1 hoặc 16:9
        val showCallToAction: Boolean = true,
        val showAdvertiser: Boolean = true
    ) : NativeAdType()
}
```

### 3.3 Implementation Blueprint

```kotlin
@Singleton
class NativeAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnitsProvider: NativeAdUnitsProvider,
    private val adsConfig: AdsConfig,
    private val vipGate: VipGate,
    private val analyticsLogger: AnalyticsLogger,
    private val adRevenueReporter: AdRevenueReporter
) {
    // Cache với max size
    private val adCache = LruCache<String, NativeAd>(3)
    
    suspend fun loadInlineAd(): NativeAd?
    suspend fun loadLargeAd(): NativeAd?
    fun preloadAds(count: Int = 2)
    fun reportImpression(ad: NativeAd, position: Int, route: String?)
}
```

### 3.4 Compose Component Sample

```kotlin
@Composable
fun NativeAdInline(
    nativeAd: NativeAd?,
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (String) -> Unit = {}
) {
    if (nativeAd == null) {
        // Shimmer placeholder
        NativeAdShimmer(height = 100.dp)
        return
    }
    
    AndroidView(
        factory = { context ->
            // Inflate native ad template
            NativeAdView(context).apply {
                // Bind ad assets
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}
```

---

## 4. Giải Pháp Tích Hợp Open App Ad

### 4.1 Architecture Đề Xuất

```
openapp/
├── OpenAppAdManager.kt         // Manager singleton
├── AppOpenAdLoader.kt          // Load và lifecycle handling
├── AppLifecycleObserver.kt     // ProcessLifecycleOwner observer
└── OpenAppAdConfig.kt          // Config (cooldown, etc.)
```

### 4.2 Lifecycle Flow

```
App Background → App Foreground
        ↓
AppLifecycleObserver.onStart()
        ↓
Check conditions:
  - isVip? → Skip
  - Cooldown passed? → Continue
  - Ad loaded? → Show
        ↓
Show Open App Ad
        ↓
onAdDismissed → Resume app
```

### 4.3 Core Implementation

```kotlin
@Singleton
class OpenAppAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnitsProvider: AdUnitsProvider,
    private val vipGate: VipGate,
    private val adsConfig: AdsConfig,
    private val analyticsLogger: AnalyticsLogger,
    private val adRevenueReporter: AdRevenueReporter
) : DefaultLifecycleObserver {
    
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var loadTime: Long = 0
    private var isShowingAd = false
    
    companion object {
        private const val AD_EXPIRATION_HOURS = 4L
        private const val COOLDOWN_SECONDS = 30L  // Giữa các lần show
    }
    
    // Attach to ProcessLifecycleOwner
    fun initialize(application: Application) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    
    override fun onStart(owner: LifecycleOwner) {
        // App comes to foreground
        showAdIfAvailable()
    }
    
    private fun showAdIfAvailable() {
        if (isShowingAd || vipGate.isVip()) return
        if (!isAdAvailable()) {
            loadAd()
            return
        }
        // Show ad...
    }
    
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo()
    }
}
```

### 4.4 Config Options

```kotlin
data class OpenAppAdConfig(
    val enabled: Boolean = true,
    val cooldownSeconds: Long = 30,           // Giữa các lần show
    val showOnColdStart: Boolean = true,      // Show khi mở app lần đầu
    val showOnWarmStart: Boolean = true,      // Show khi từ background lên
    val excludedActivities: Set<String> = setOf(
        "SplashActivity",
        "PaymentActivity",
        "PremiumActivity"
    ),
    val maxDailyShows: Int = 10               // Giới hạn hàng ngày
)
```

---

## 5. Kết Hợp SmartInterstitial + VIP Logic

### 5.1 Phân Tích Hiện Tại

**SmartInterstitial đã có:**
- ✅ `maybeShow()` - Kiểm tra policy trước khi show
- ✅ `show()` - Force show bypass policy
- ✅ Cooldown, daily cap, route blocklist
- ✅ First launch delay

**VIP Logic đã có:**
- ✅ `VipGate` interface
- ✅ Check trong `preload()` và `passPolicy()`
- ⚠️ Chưa có dynamic VIP update

### 5.2 Giải Pháp Kết Hợp Nâng Cao

#### **Option A: Reactive VIP (Recommend cho Revenue)**

```kotlin
// Enhanced VipGate interface
interface VipGate {
    fun isVip(): Boolean
    
    // NEW: Reactive flow
    val vipStatusFlow: StateFlow<Boolean>
    
    // NEW: Grace period (hiện ads trong X phút sau khi hết VIP)
    fun getGracePeriodRemaining(): Long
}

// VIP-aware ad showing
suspend fun maybeShowWithVipLogic(
    activity: Activity,
    currentRoute: String?
): ShowResult {
    return when {
        vipGate.isVip() -> ShowResult.VipUser
        vipGate.getGracePeriodRemaining() > 0 -> {
            // Grace period: show but less frequent
            if (shouldShowInGracePeriod()) {
                showAd(activity, currentRoute)
            } else {
                ShowResult.GracePeriodSkip
            }
        }
        else -> showAd(activity, currentRoute)
    }
}
```

#### **Option B: Smart Frequency Capping by User Segment**

```kotlin
data class UserSegment(
    val isVip: Boolean,
    val isTrialUser: Boolean,
    val daysInstalled: Int,
    val sessionCount: Int,
    val purchaseHistory: Boolean
)

fun getInterstitialCooldown(segment: UserSegment): Long {
    return when {
        segment.isVip -> Long.MAX_VALUE  // Never show
        segment.isTrialUser -> 60_000L   // 1 min (lower frequency during trial)
        segment.daysInstalled < 3 -> 30_000L  // New users: be gentle
        segment.purchaseHistory -> 45_000L    // Paying users: medium
        else -> 15_000L  // Free users: standard
    }
}
```

### 5.3 Smart Show Strategy Matrix

| User Type | Interstitial | Banner | Native | Open App |
|-----------|-------------|--------|--------|----------|
| **VIP Active** | ❌ Never | ❌ Never | ❌ Never | ❌ Never |
| **VIP Expired (Grace)** | 🟡 Low freq | ✅ Show | ✅ Show | ❌ No |
| **Trial User** | 🟡 Medium | ✅ Show | ✅ Show | 🟡 Low |
| **New User (<3 days)** | 🟡 Low freq | ✅ Show | ✅ Show | ❌ No |
| **Free User** | ✅ Normal | ✅ Show | ✅ Show | ✅ Show |

---

## 6. ProGuard & Production Readiness

### 6.1 Trạng Thái ProGuard Hiện Tại

**✅ Đã đầy đủ cho:**
- AdMob SDK
- Vungle, IronSource, Meta, InMobi adapters
- Firebase Analytics
- Hilt/Dagger
- ILRD listeners

### 6.2 Cần Bổ Sung Cho Native & Open App Ad

```proguard
# ============================================================================
# Native Ad Support (ADD TO consumer-rules.pro)
# ============================================================================
-keep class com.google.android.gms.ads.nativead.** { *; }
-keep class com.google.android.gms.ads.nativead.NativeAd$* { *; }
-keepclassmembers class * implements com.google.android.gms.ads.nativead.NativeAd$OnNativeAdLoadedListener {
    <methods>;
}

# ============================================================================
# Open App Ad Support (ADD TO consumer-rules.pro)  
# ============================================================================
-keep class com.google.android.gms.ads.appopen.** { *; }
-keep class com.google.android.gms.ads.appopen.AppOpenAd$* { *; }
-keepclassmembers class * implements com.google.android.gms.ads.appopen.AppOpenAd$AppOpenAdLoadCallback {
    <methods>;
}

# ProcessLifecycleOwner for Open App Ad
-keep class androidx.lifecycle.ProcessLifecycleOwner { *; }
-keep class * implements androidx.lifecycle.DefaultLifecycleObserver { *; }
```

### 6.3 Production Readiness Checklist

| Item | Status | Action |
|------|--------|--------|
| Test Ad Unit IDs | ⚠️ Using test | Replace với production IDs |
| AdMob App ID | ⚠️ Check | Verify trong AndroidManifest |
| Network Security Config | ✅ OK | Cleartext allowed for test |
| Mediation Adapters | ✅ All bidding | No action needed |
| ProGuard Rules | ⚠️ Incomplete | Add Native/OpenApp rules |
| Analytics Events | ✅ Comprehensive | No action needed |
| ILRD Tracking | ✅ Complete | No action needed |
| App-ads.txt | ❓ Unknown | Verify trên server |
| GDPR/Consent | ❓ Missing | Implement UMP SDK |
| Ad Unit IDs Provider | ⚠️ Test mode | Create ProductionAdUnitsProvider |

### 6.4 Cần Thêm Để Production Ready

```kotlin
// 1. Extended AdUnitsProvider interface
interface AdUnitsProvider {
    val bannerAdUnitId: String
    val interstitialAdUnitId: String
    
    // NEW
    val nativeAdUnitId: String
    val openAppAdUnitId: String
    val rewardedAdUnitId: String  // Future
}

// 2. GDPR/Consent (UMP SDK)
// Thêm dependency: com.google.android.ump:user-messaging-platform
class ConsentManager @Inject constructor() {
    fun requestConsentInfoUpdate(activity: Activity)
    fun loadAndShowConsentFormIfRequired(activity: Activity)
}
```

---

## 7. Options Ưu Tiên Doanh Thu

### 🏆 Option 1: Maximum Revenue (Khuyến nghị)

**Ưu tiên:** Native Large → Open App → Interstitial → Banner

| Ad Format | Priority | Est. eCPM | Implementation |
|-----------|----------|-----------|----------------|
| Native Large Height | 1 | $8-15 | Article end, standalone sections |
| Open App Ad | 2 | $10-20 | Background → Foreground |
| Interstitial | 3 | $5-12 | Smart timing (existing) |
| Native Inline | 4 | $3-6 | Feed items, list spacing |
| Banner | 5 | $1-3 | Bottom persistent |

**Revenue Estimate:** +40-60% so với hiện tại

---

### ⚖️ Option 2: Balanced UX (Cân bằng)

**Ưu tiên:** User Experience + Revenue

| Ad Format | Frequency | Notes |
|-----------|-----------|-------|
| Banner | Always visible | Auto-refresh 45s |
| Native Inline | Every 5-8 items | Feed integration |
| Interstitial | 2-3x/session | Smart timing |
| Open App | 1x/session | First foreground only |
| Native Large | 1x/article | End of content |

**Revenue Estimate:** +25-35% so với hiện tại

---

### 🚀 Option 3: Aggressive (Short-term Revenue)

**Ưu tiên:** Maximum short-term revenue (may impact retention)

| Ad Format | Strategy |
|-----------|----------|
| Open App | Every foreground (30s cooldown) |
| Interstitial | Every navigation |
| Native | Every 3 items + article end |
| Banner | Always + refresh 30s |

**Revenue Estimate:** +80-100% (⚠️ Risk: -20% retention)

---

## 8. Implementation Roadmap

### Phase 1: Native Ad (Week 1-2)
```
1. Create NativeAdManager.kt
2. Create NativeAdLoader.kt with caching
3. Implement NativeAdInline composable
4. Implement NativeAdLargeHeight composable
5. Add ILRD tracking for native
6. Update AdUnitsProvider interface
7. Update ProGuard rules
8. Testing & QA
```

### Phase 2: Open App Ad (Week 2-3)
```
1. Create OpenAppAdManager.kt
2. Implement AppLifecycleObserver
3. Add ProcessLifecycleOwner integration
4. Implement cooldown logic
5. Add ILRD tracking
6. Update ProGuard rules
7. Testing across all activities
8. QA lifecycle edge cases
```

### Phase 3: Smart VIP Integration (Week 3-4)
```
1. Enhance VipGate interface with reactive flow
2. Implement grace period logic
3. Create UserSegment-based frequency capping
4. Update all ad managers with new VIP logic
5. Add analytics for VIP conversion tracking
6. Testing VIP transitions
```

### Phase 4: Production Polish (Week 4-5)
```
1. Implement GDPR/UMP consent
2. Create ProductionAdUnitsProvider
3. Finalize ProGuard rules
4. Performance optimization
5. Crash reporting integration
6. Final QA & Production release
```

---

## 📝 Kết Luận

### Điểm Mạnh Hiện Tại
- ✅ Bidding mediation setup đúng chuẩn
- ✅ ILRD tracking hoàn chỉnh
- ✅ Clean architecture với DI
- ✅ VIP logic framework có sẵn

### Cần Cải Thiện
- ❌ Thiếu Native Ad (High revenue potential)
- ❌ Thiếu Open App Ad (High eCPM)
- ⚠️ VIP logic chưa reactive
- ⚠️ GDPR consent chưa có

### Khuyến Nghị
> **Chọn Option 1 (Maximum Revenue)** với roadmap 4-5 tuần để đạt revenue tối ưu trong khi vẫn duy trì trải nghiệm người dùng chấp nhận được.

---

*Document generated: 25/12/2024*
*Module version: base-ads 1.0*
