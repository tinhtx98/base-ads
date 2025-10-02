# 📱 Hướng Dẫn Cấu Hình AdMob & ironSource

## 🎯 Phân Biệt AdMob và ironSource

### **1. AdMob (Google Mobile Ads)**

#### A. AdMob App ID
- **Định dạng**: `ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX` (có dấu `~`)
- **Vị trí**: `AndroidManifest.xml`
- **Ví dụ của bạn**: `ca-app-pub-8819120490234533~8638521114` ✅

```xml
<!-- AndroidManifest.xml -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-8819120490234533~8638521114"/>
```

#### B. AdMob Ad Unit IDs
- **Định dạng**: `ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX` (có dấu `/`)
- **Vị trí**: Code trong `SampleAppAdsModule.kt`
- **Các loại**:
  - **Banner Ad Unit**: `ca-app-pub-8819120490234533/8043624743`
  - **Interstitial Ad Unit**: `ca-app-pub-8819120490234533/XXXXXXXXXX` (cần tạo)
  - **Rewarded Ad Unit**: `ca-app-pub-8819120490234533/XXXXXXXXXX` (cần tạo)

```kotlin
// SampleAppAdsModule.kt
ProductionAdUnitsProvider(
    bannerUnitId = "ca-app-pub-8819120490234533/8043624743",
    interstitialUnitId = "ca-app-pub-8819120490234533/XXXXXXXXXX", // Tạo mới
    rewardedUnitId = "ca-app-pub-8819120490234533/XXXXXXXXXX" // Optional
)
```

### **2. ironSource Mediation**

#### ironSource App Key
- **Định dạng**: Chuỗi 5-10 ký tự (KHÔNG PHẢI AdMob ID)
- **Vị trí**: Code trong `SampleAppAdsModule.kt`
- **Ví dụ**: `23b463c45` hoặc `abc12345`

```kotlin
// SampleAppAdsModule.kt
AdsConfig(
    ironSourceAppKey = "23b463c45", // ironSource key của bạn (5-10 chars)
    enableIronSourceLogging = true
)
```

## 🚨 Lỗi Thường Gặp

### **Lỗi 1: Nhầm AdMob Ad Unit ID với ironSource App Key**

❌ **SAI**:
```kotlin
AdsConfig(
    ironSourceAppKey = "ca-app-pub-8819120490234533/8043624743" // SAI! Đây là AdMob ID
)
```

✅ **ĐÚNG**:
```kotlin
AdsConfig(
    ironSourceAppKey = "23b463c45" // ĐÚNG! ironSource key 5-10 chars
)
```

### **Lỗi 2: Nhầm AdMob App ID với Ad Unit ID**

❌ **SAI**:
```kotlin
ProductionAdUnitsProvider(
    bannerUnitId = "ca-app-pub-8819120490234533~8638521114" // SAI! Đây là App ID (có ~)
)
```

✅ **ĐÚNG**:
```kotlin
ProductionAdUnitsProvider(
    bannerUnitId = "ca-app-pub-8819120490234533/8043624743" // ĐÚNG! Ad Unit ID (có /)
)
```

## 📋 Checklist Cấu Hình

### ✅ Bước 1: Lấy AdMob IDs từ AdMob Console

1. Đăng nhập [AdMob Console](https://apps.admob.com/)
2. Chọn app của bạn
3. Lấy **App ID**: `ca-app-pub-XXXXXXXX~XXXXXXXX`
4. Tạo **Ad Units**:
   - Banner Ad Unit
   - Interstitial Ad Unit
   - Rewarded Ad Unit (optional)

### ✅ Bước 2: Cấu Hình AndroidManifest.xml

```xml
<application>
    <!-- AdMob App ID (có dấu ~) -->
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-8819120490234533~8638521114"/>
</application>
```

### ✅ Bước 3: Cấu Hình Ad Unit IDs trong Code

```kotlin
// app/src/main/java/com/tinhtx/baseads/di/SampleAppAdsModule.kt

@Provides
@Singleton
fun provideSampleAdUnitsProvider(): AdUnitsProvider {
    return ProductionAdUnitsProvider(
        // 🎯 Ad Unit IDs từ AdMob Console (có dấu /)
        bannerUnitId = "ca-app-pub-8819120490234533/8043624743",
        interstitialUnitId = "ca-app-pub-8819120490234533/XXXXXXXXXX",
        rewardedUnitId = "ca-app-pub-8819120490234533/XXXXXXXXXX"
    )
}
```

### ✅ Bước 4: Lấy ironSource App Key

1. Đăng ký tài khoản [ironSource](https://platform.ironsrc.com/)
2. Tạo app mới
3. Lấy **App Key** (5-10 ký tự): ví dụ `23b463c45`

### ✅ Bước 5: Cấu Hình ironSource App Key

```kotlin
// app/src/main/java/com/tinhtx/baseads/di/SampleAppAdsModule.kt

@Provides
@Singleton
fun provideSampleAdsConfig(): AdsConfig {
    return AdsConfig(
        ironSourceAppKey = "23b463c45", // ironSource key (5-10 chars)
        enableIronSourceLogging = BuildConfig.DEBUG
    )
}
```

## 🎯 Cấu Hình Đầy Đủ Của Bạn

```kotlin
// SampleAppAdsModule.kt

@Module
@InstallIn(SingletonComponent::class)
object SampleAppAdsModule {
    
    @Provides
    @Singleton
    fun provideSampleAdsConfig(): AdsConfig {
        return AdsConfig(
            enableAds = true,
            enableInterstitial = true,
            enableBanner = true,
            
            // ⚡ ironSource App Key (5-10 chars)
            ironSourceAppKey = "23b463c45", // Lấy từ ironSource Dashboard
            enableIronSourceLogging = BuildConfig.DEBUG
        )
    }
    
    @Provides
    @Singleton
    fun provideSampleAdUnitsProvider(): AdUnitsProvider {
        return ProductionAdUnitsProvider(
            // 📱 AdMob Ad Unit IDs (có dấu /)
            bannerUnitId = "ca-app-pub-8819120490234533/8043624743",
            interstitialUnitId = "ca-app-pub-8819120490234533/XXXXXXXXXX", // Tạo mới
            rewardedUnitId = "ca-app-pub-8819120490234533/XXXXXXXXXX" // Optional
        )
    }
}
```

```xml
<!-- AndroidManifest.xml -->
<application>
    <!-- 📱 AdMob App ID (có dấu ~) -->
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-8819120490234533~8638521114"/>
</application>
```

## 🔍 Cách Lấy Ad Unit IDs Từ AdMob

### Bước 1: Truy cập AdMob Console
```
https://apps.admob.com/
```

### Bước 2: Chọn App
1. Click vào app "Base Ads" của bạn
2. Hoặc tạo app mới nếu chưa có

### Bước 3: Tạo Ad Units
1. Click "Ad units" trong menu
2. Click "Add ad unit"
3. Chọn loại:
   - **Banner** → Nhận ID dạng `ca-app-pub-XXXX/XXXX`
   - **Interstitial** → Nhận ID dạng `ca-app-pub-XXXX/XXXX`
   - **Rewarded** → Nhận ID dạng `ca-app-pub-XXXX/XXXX`

### Bước 4: Copy Ad Unit IDs
```
Banner: ca-app-pub-8819120490234533/8043624743 ✅ (đã có)
Interstitial: ca-app-pub-8819120490234533/XXXXXXXXXX (cần tạo)
Rewarded: ca-app-pub-8819120490234533/XXXXXXXXXX (cần tạo)
```

## 🚀 Test Ads Sau Khi Cấu Hình

### 1. Rebuild Project
```bash
./gradlew clean build
```

### 2. Run App
```bash
./gradlew installDebug
```

### 3. Check Logs
```bash
adb logcat | grep -E "(BaseAds|Ads|ironSource)"
```

### 4. Expected Success Logs
```
✅ BaseAds-Initializer: ironSource initialization completed
✅ BaseAds-Initializer: Adapter: IronSourceMediationAdapter, Status: READY
✅ BaseAds-Banner: Banner ad loaded successfully
✅ BaseAds-Interstitial: Interstitial ad loaded
```

## ⚠️ Lưu Ý Quan Trọng

### 1. **Test vs Production Ads**
- **Test Ads** (Google sample IDs): `ca-app-pub-3940256099942544/...`
- **Your Real Ads**: `ca-app-pub-8819120490234533/...`

### 2. **ironSource App Key**
- Chỉ nhận key từ **ironSource Dashboard**
- **KHÔNG** sử dụng AdMob IDs làm ironSource key

### 3. **Ad Unit IDs**
- Mỗi loại ad (banner, interstitial, rewarded) có ID riêng
- Tạo từ **AdMob Console**

### 4. **App ID vs Ad Unit ID**
- **App ID** (có `~`): Đặt trong `AndroidManifest.xml`
- **Ad Unit ID** (có `/`): Đặt trong code

## 🎉 Kết Luận

**Sau khi cấu hình đúng:**
1. ✅ AdMob App ID trong manifest
2. ✅ AdMob Ad Unit IDs trong code
3. ✅ ironSource App Key trong code
4. ✅ ironSource mediation adapter sẽ status READY
5. ✅ Ads sẽ load và hiển thị bình thường!

**Nếu vẫn còn lỗi "No fill":**
- Test trên thiết bị thật (không phải emulator)
- Đảm bảo có kết nối internet tốt
- Chờ vài giờ sau khi tạo ad units mới trong AdMob