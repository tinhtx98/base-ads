# InMobi Bidding Mediation Setup Guide

## 📋 Overview

Hướng dẫn tích hợp InMobi bidding adapter vào BaseAds library và AdMob mediation.

## ✅ Đã tích hợp trong BaseAds v1.0.0+

- ✅ InMobi Mediation Adapter: **10.8.7.0**
- ✅ Dependency scope: `api` (tự động export qua AAR)
- ✅ ProGuard rules: Tự động áp dụng từ consumer-rules.pro
- ✅ BuildConfig: `INMOBI_PARTNER = "inmobi"`

## 🚀 Setup trong AdMob Console

### 1. Tạo InMobi Mediation Group

1. Vào **AdMob Console** → **Mediation** → **Create mediation group**
2. Chọn ad format (Banner/Interstitial/Rewarded)
3. Platform: **Android**
4. Targeting: Theo nhu cầu

### 2. Thêm InMobi Ad Source

1. Click **"Add ad sources"**
2. Chọn **InMobi** từ danh sách
3. **Bidding**: Chọn **"Set up bidding"** (QUAN TRỌNG!)
4. Nhập **InMobi Placement ID** từ InMobi dashboard

### 3. Cấu hình Bidding

```
Mediation Group
├── Ad Unit ID: ca-app-pub-xxxxx~xxxxxx
├── Ad Sources (Bidding):
│   ├── AdMob Network (Always enabled)
│   ├── Meta Audience Network (Bidding)
│   ├── IronSource (Bidding)
│   ├── InMobi (Bidding) ← New!
│   └── Vungle/LiftOff (Bidding)
└── eCPM floor: $0.01 (tùy chọn)
```

## 🔑 Lấy InMobi Placement ID

### InMobi Dashboard

1. Đăng nhập: https://www.inmobi.com/publisher/
2. Vào **Inventory** → **Placements**
3. Tạo placement mới hoặc copy ID của placement có sẵn
4. Copy **Placement ID** (format: 1234567890123456789)

### Placement Types

- **Banner**: 320x50, 300x250, 728x90
- **Interstitial**: Full screen
- **Rewarded Video**: Video with rewards
- **Native**: Custom native ads

## 📱 Integration trong Primary Project

### 1. Dependencies (Tự động từ AAR)

```kotlin
// base-ads.aar đã bao gồm InMobi adapter
dependencies {
    implementation(files("libs/base-ads.aar"))
    
    // Các dependency này được auto-export từ AAR qua 'api' scope
    // ✅ com.google.ads.mediation:inmobi:10.8.7.0
    // ✅ com.inmobi.monetization:inmobi-ads (transitive)
}
```

### 2. Ad Unit Configuration

```kotlin
// AdsModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AdsModule {
    @Provides
    @Singleton
    fun provideBannerAdUnitId(): String {
        // AdMob ad unit ID (có InMobi trong mediation group)
        return "ca-app-pub-xxxxx/yyyyyyy"
    }
}
```

### 3. ProGuard Rules (Tự động!)

Consumer rules từ AAR tự động bảo vệ InMobi:

```proguard
# InMobi Mediation Adapter
-keep class com.google.ads.mediation.inmobi.** { *; }
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**

# InMobi SDK classes
-keep class com.inmobi.ads.** { *; }
-keep interface com.inmobi.ads.** { *; }

# InMobi internal classes
-keep class com.inmobi.media.** { *; }
-dontwarn com.inmobi.media.**
```

## 🧪 Testing InMobi Adapter

### 1. Enable Test Mode

```kotlin
// Application onCreate
MobileAds.setRequestConfiguration(
    RequestConfiguration.Builder()
        .setTestDeviceIds(listOf("YOUR_TEST_DEVICE_ID"))
        .build()
)
```

### 2. Check Adapter Status

```kotlin
// Debug build
adb logcat | grep -E "(InMobi|Mediation)"
```

Expected output:
```
✅ com.google.ads.mediation.inmobi.InMobiMediationAdapter: READY (500ms)
```

### 3. Verify eCPM in AdMob

1. Load test ad trong app
2. Vào **AdMob Console** → **Mediation** → **Ad sources**
3. Kiểm tra **InMobi** status: **READY**
4. Sau vài test impressions, xem eCPM values

## 🔍 Debugging

### Adapter Not Loading

```bash
# Check if InMobi adapter exists in classpath
adb shell pm list packages | grep inmobi

# Should see:
# com.inmobi.sdk (if standalone app)
# or check logcat for InMobiMediationAdapter
```

### No Fill from InMobi

Nguyên nhân phổ biến:
- ❌ Placement ID sai hoặc chưa kích hoạt
- ❌ App chưa được approve trong InMobi dashboard
- ❌ Geo-targeting không khớp
- ❌ Inventory hết (low fill rate)

### eCPM = "-" trong Release Build

**ĐÃ FIX!** Consumer rules tự động bảo vệ InMobi adapter.

Nếu vẫn gặp vấn đề:
1. Verify AAR mới nhất đã được copy
2. Clean rebuild project: `./gradlew clean assembleRelease`
3. Check ProGuard mapping: `app/build/outputs/mapping/release/configuration.txt`

## 📊 Expected Mediation Waterfall

```
Request → AdMob Bidding Auction
           ↓
    ┌─────────────────────┐
    │  All Bidders Send   │
    │  Real-time Bids     │
    ├─────────────────────┤
    │ • AdMob Network     │
    │ • Meta (Facebook)   │
    │ • IronSource        │
    │ • InMobi ← NEW!    │
    │ • Vungle (LiftOff)  │
    └─────────────────────┘
           ↓
    Highest Bid Wins
           ↓
    Ad Displayed
           ↓
    eCPM Tracked ✅
```

## 🎯 Best Practices

### 1. Placement ID Management

```kotlin
// BuildConfig hoặc Remote Config
object InMobiConfig {
    const val BANNER_PLACEMENT_ID = "1234567890123456789"
    const val INTERSTITIAL_PLACEMENT_ID = "9876543210987654321"
}
```

### 2. Monitor Performance

- Track **Fill Rate**: InMobi vs others
- Compare **eCPM**: Adjust floor price nếu cần
- Monitor **Latency**: Bid response time

### 3. A/B Testing

Test InMobi vs other networks:
- Group A: AdMob + Meta + IronSource + Vungle
- Group B: AdMob + Meta + IronSource + Vungle + **InMobi**

Compare revenue per user (ARPU).

## ✅ Verification Checklist

- [ ] InMobi adapter version: 10.8.7.0 trong AAR
- [ ] Placement ID đã tạo trong InMobi dashboard
- [ ] Mediation group có InMobi bidding source
- [ ] Test ad hiển thị thành công
- [ ] Adapter status = READY trong logs
- [ ] eCPM != "-" trong AdMob reporting (sau vài test impressions)
- [ ] Release build không bị strip adapter (ProGuard working)

## 📚 Resources

- **InMobi Developer Docs**: https://support.inmobi.com/monetization/
- **AdMob InMobi Adapter**: https://developers.google.com/admob/android/mediation/inmobi
- **Bidding Setup**: https://support.google.com/admob/answer/9234653

---

**Version**: 1.0.0  
**Last Updated**: November 13, 2025  
**InMobi Adapter**: 10.8.7.0
