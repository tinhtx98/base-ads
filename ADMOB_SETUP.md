# Hướng dẫn cấu hình AdMob ID

## 📍 **AdMob ID được đặt ở đâu?**

AdMob ID (Ad Unit ID) được cấu hình thông qua **`AdUnitsProvider`** interface và có thể được setup theo nhiều cách khác nhau:

## 🔧 **Cách 1: Cấu hình trong Custom Module (Khuyến nghị)**

```kotlin
// app/src/main/java/com/yourpackage/di/YourAdsModule.kt

@Module
@InstallIn(SingletonComponent::class)
object YourAdsModule {
    
    @Provides
    @Singleton
    fun provideAdUnitsProvider(): AdUnitsProvider {
        return ProductionAdUnitsProvider(
            // 🎯 ĐÂY LÀ NƠI ĐẶT ADMOB AD UNIT IDS
            bannerUnitId = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX", // ⬅️ Banner Ad Unit ID
            interstitialUnitId = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX" // ⬅️ Interstitial Ad Unit ID
        )
    }
}
```

## 🔧 **Cách 2: Cấu hình qua Build Variants**

### 2.1. Trong `build.gradle.kts` (app module):

```kotlin
android {
    buildTypes {
        debug {
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
        }
        release {
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-YOUR-REAL-BANNER-ID\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-YOUR-REAL-INTERSTITIAL-ID\"")
        }
    }
}
```

### 2.2. Sử dụng trong AdUnitsProvider:

```kotlin
@Provides
@Singleton
fun provideAdUnitsProvider(): AdUnitsProvider {
    return ProductionAdUnitsProvider(
        bannerUnitId = BuildConfig.BANNER_AD_UNIT_ID,
        interstitialUnitId = BuildConfig.INTERSTITIAL_AD_UNIT_ID
    )
}
```

## 🔧 **Cách 3: Cấu hình qua local.properties (Bảo mật)**

### 3.1. Thêm vào `local.properties`:

```properties
# local.properties (file này không được commit lên git)
admob.banner.debug=ca-app-pub-3940256099942544/6300978111
admob.interstitial.debug=ca-app-pub-3940256099942544/1033173712
admob.banner.release=ca-app-pub-YOUR-REAL-BANNER-ID
admob.interstitial.release=ca-app-pub-YOUR-REAL-INTERSTITIAL-ID
```

### 3.2. Đọc trong `build.gradle.kts`:

```kotlin
// app/build.gradle.kts
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    buildTypes {
        debug {
            buildConfigField("String", "BANNER_AD_UNIT_ID", 
                "\"${localProperties.getProperty("admob.banner.debug", "")}\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", 
                "\"${localProperties.getProperty("admob.interstitial.debug", "")}\"")
        }
        release {
            buildConfigField("String", "BANNER_AD_UNIT_ID", 
                "\"${localProperties.getProperty("admob.banner.release", "")}\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", 
                "\"${localProperties.getProperty("admob.interstitial.release", "")}\"")
        }
    }
}
```

## 🔧 **Cách 4: Custom AdUnitsProvider Implementation**

```kotlin
// Tạo custom implementation
class MyAppAdUnitsProvider @Inject constructor(
    private val remoteConfig: RemoteConfig // Hoặc secure storage
) : AdUnitsProvider {
    
    override val bannerAdUnitId: String
        get() = remoteConfig.getString("banner_ad_unit_id") 
            .takeIf { it.isNotBlank() } 
            ?: AdsConstants.TestAdUnits.BANNER
    
    override val interstitialAdUnitId: String
        get() = remoteConfig.getString("interstitial_ad_unit_id")
            .takeIf { it.isNotBlank() }
            ?: AdsConstants.TestAdUnits.INTERSTITIAL
}

// Bind trong module
@Binds
@Singleton
abstract fun bindAdUnitsProvider(
    myAppAdUnitsProvider: MyAppAdUnitsProvider
): AdUnitsProvider
```

## 🔧 **Cách 5: Flavors cho Multiple Apps**

```kotlin
// build.gradle.kts
android {
    productFlavors {
        create("appA") {
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-AAAA/BBBB\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-AAAA/CCCC\"")
        }
        create("appB") {
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-DDDD/EEEE\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-DDDD/FFFF\"")
        }
    }
}
```

## 📋 **Lấy AdMob Ad Unit ID ở đâu?**

### 1. **Đăng nhập AdMob Console:**
   - Truy cập: [https://apps.admob.com](https://apps.admob.com)
   - Đăng nhập Google account có quyền truy cập AdMob

### 2. **Tạo hoặc chọn App:**
   - Nếu chưa có: "Apps" → "Add App" → Chọn platform Android
   - Nhập package name và tên app
   - Nếu đã có: Chọn app từ danh sách

### 3. **Tạo Ad Units:**
   
   #### **Banner Ad Unit:**
   - "Ad units" → "Add ad unit"
   - Chọn "Banner"
   - Đặt tên ad unit (ví dụ: "Main Banner")
   - Chọn ad format: Adaptive banner
   - Copy **Ad unit ID** (dạng: `ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY`)

   #### **Interstitial Ad Unit:**
   - "Ad units" → "Add ad unit"
   - Chọn "Interstitial"
   - Đặt tên ad unit (ví dụ: "Navigation Interstitial")
   - Copy **Ad unit ID**

### 4. **Test Ad Unit IDs (Development):**
   - **Banner Test ID:** `ca-app-pub-3940256099942544/6300978111`
   - **Interstitial Test ID:** `ca-app-pub-3940256099942544/1033173712`

## ✅ **Default Configuration (hiện tại)**

Module hiện tại sử dụng `TestAdUnitsProvider` với Google test IDs:

```kotlin
// base-ads/src/main/java/com/tinhtx/baseads/core/AdsConstants.kt
object TestAdUnits {
    const val BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
}

// Default binding trong AdsModule
@Binds
@Singleton
abstract fun bindAdUnitsProvider(
    testAdUnitsProvider: TestAdUnitsProvider // ⬅️ Sử dụng test IDs
): AdUnitsProvider
```

## 🔄 **Override để sử dụng Production IDs**

Để sử dụng real ad unit IDs, override binding trong app module:

```kotlin
// app/src/main/java/com/yourapp/di/YourAdsModule.kt
@Module
@InstallIn(SingletonComponent::class)
object YourAdsModule {
    
    // Override default TestAdUnitsProvider
    @Provides
    @Singleton
    fun provideAdUnitsProvider(): AdUnitsProvider {
        return ProductionAdUnitsProvider(
            bannerUnitId = "ca-app-pub-YOUR-BANNER-ID",
            interstitialUnitId = "ca-app-pub-YOUR-INTERSTITIAL-ID"
        )
    }
}
```

## 🚨 **Lưu ý quan trọng**

### **🔒 Bảo mật:**
- ❌ **KHÔNG** hardcode production ad unit IDs trực tiếp trong code
- ✅ **SỬ DỤNG** BuildConfig, local.properties, hoặc remote config
- ✅ **KHÁC BIỆT** test và production IDs
- ✅ **KHÔNG COMMIT** local.properties lên git

### **⚡ Test vs Production:**
- **Development:** Luôn sử dụng test ad unit IDs của Google
- **Production:** Chỉ sử dụng real ad unit IDs trong release builds
- **Staging:** Có thể sử dụng test IDs hoặc sandbox ad units

### **📊 Tracking:**
- Mỗi ad unit có analytics riêng trong AdMob console
- Đặt tên ad units rõ ràng để dễ tracking
- Monitor performance của từng ad unit

## 🔍 **Verification**

### **Kiểm tra cấu hình hiện tại:**

```kotlin
// Inject AdUnitsProvider để kiểm tra
@Inject
lateinit var adUnitsProvider: AdUnitsProvider

// Log ad unit IDs
Log.d("ADS", "Banner ID: ${adUnitsProvider.bannerAdUnitId}")
Log.d("ADS", "Interstitial ID: ${adUnitsProvider.interstitialAdUnitId}")
```

### **Trong Demo App:**

Demo app sẽ hiển thị ad unit IDs hiện tại trong debug information:
- ✅ `Banner Ad Unit: ca-app-pub-394...`
- ✅ `Interstitial Ad Unit: ca-app-pub-394...`

## 📞 **Troubleshooting**

### **Ads không hiển thị:**
1. ✅ Kiểm tra ad unit ID có đúng format không
2. ✅ Verify app đã được approve trong AdMob console
3. ✅ Check internet connection
4. ✅ Đảm bảo sử dụng test IDs khi develop

### **Invalid ad unit ID:**
1. ✅ Copy chính xác từ AdMob console
2. ✅ Không có extra spaces hoặc ký tự đặc biệt
3. ✅ Format đúng: `ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY`

### **Ad serving limited:**
1. ✅ Chờ 24-48h sau khi tạo ad units mới
2. ✅ Verify app compliance với AdMob policies
3. ✅ Check app đã publish trên Play Store (cho production ads)

---

**🎯 AdMob ID configuration hoàn tất! Module Base Ads sẵn sàng với production ad units.**