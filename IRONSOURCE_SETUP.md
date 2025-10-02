# Hướng dẫn cấu hình ironSource App ID

## 📍 **ironSource App ID được đặt ở đâu?**

ironSource App Key được cấu hình trong **`AdsConfig`** và có thể được set theo nhiều cách khác nhau:

## 🔧 **Cách 1: Cấu hình trong Custom Module (Khuyến nghị)**

```kotlin
// app/src/main/java/com/yourpackage/di/YourAdsModule.kt

@Module
@InstallIn(SingletonComponent::class)
object YourAdsModule {
    
    @Provides
    @Singleton
    fun provideAdsConfig(): AdsConfig {
        return AdsConfig(
            enableAds = true,
            enableInterstitial = true,
            enableBanner = true,
            
            // 🎯 ĐÂY LÀ NƠI ĐẶT IRONSOURCE APP KEY
            ironSourceAppKey = "YOUR_IRONSOURCE_APP_KEY", // ⬅️ Thay bằng app key thật
            enableIronSourceLogging = BuildConfig.DEBUG, // Enable logging cho debug build
            
            interstitialBlocklistRoutes = setOf(
                "premium", "checkout", "payment"
            ),
            showInterstitialBeforeNavigate = false
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
            buildConfigField("String", "IRONSOURCE_APP_KEY", "\"YOUR_DEBUG_APP_KEY\"")
        }
        release {
            buildConfigField("String", "IRONSOURCE_APP_KEY", "\"YOUR_PRODUCTION_APP_KEY\"")
        }
    }
}
```

### 2.2. Sử dụng trong AdsConfig:

```kotlin
@Provides
@Singleton
fun provideAdsConfig(): AdsConfig {
    return AdsConfig(
        // Các config khác...
        ironSourceAppKey = BuildConfig.IRONSOURCE_APP_KEY,
        enableIronSourceLogging = BuildConfig.DEBUG
    )
}
```

## 🔧 **Cách 3: Cấu hình qua local.properties (Bảo mật)**

### 3.1. Thêm vào `local.properties`:

```properties
# local.properties (file này không được commit lên git)
ironsource.app.key.debug=YOUR_DEBUG_APP_KEY
ironsource.app.key.release=YOUR_PRODUCTION_APP_KEY
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
            buildConfigField("String", "IRONSOURCE_APP_KEY", 
                "\"${localProperties.getProperty("ironsource.app.key.debug", "")}\"")
        }
        release {
            buildConfigField("String", "IRONSOURCE_APP_KEY", 
                "\"${localProperties.getProperty("ironsource.app.key.release", "")}\"")
        }
    }
}
```

## 🚀 **Cách 4: Cấu hình Runtime (Linh hoạt)**

```kotlin
// Trong Application class
@HiltAndroidApp
class YourApplication : Application() {
    
    @Inject
    lateinit var adsInitializer: AdsInitializer
    
    override fun onCreate() {
        super.onCreate()
        
        // Custom configuration
        val customConfig = AdsConfig(
            enableAds = true,
            ironSourceAppKey = getIronSourceAppKey(), // Custom logic
            enableIronSourceLogging = BuildConfig.DEBUG
        )
        
        // Override default config nếu cần
        adsInitializer.initialize(this)
    }
    
    private fun getIronSourceAppKey(): String? {
        // Logic custom để lấy app key
        // Ví dụ: từ remote config, secure storage, etc.
        return when {
            BuildConfig.DEBUG -> "debug_app_key"
            else -> "production_app_key"
        }
    }
}
```

## 📋 **Lấy ironSource App Key ở đâu?**

### 1. **Đăng nhập ironSource Console:**
   - Truy cập: [https://platform.ironsrc.com](https://platform.ironsrc.com)
   - Đăng nhập tài khoản của bạn

### 2. **Tạo App:**
   - Chọn "Apps" → "Add New App"
   - Chọn platform: Android
   - Nhập thông tin app (package name, tên app)

### 3. **Copy App Key:**
   - Sau khi tạo app, vào dashboard app
   - Tìm **"App Key"** trong phần App Settings
   - Copy app key (dạng: `1234567890`)

## 🔧 **Enable ironSource Dependencies**

### 1. Uncomment dependencies trong `base-ads/build.gradle.kts`:

```kotlin
dependencies {
    // ironSource Mediation
    implementation("com.ironsrc.mediationsdk:mediationsdk:8.4.0")
    implementation("com.google.ads.mediation:ironsource:8.4.0.0")
}
```

### 2. Thêm repository trong `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://android-sdk.is.com/") // ironSource repository
    }
}
```

### 3. Update imports trong `AdsInitializer.kt`:

```kotlin
// Uncomment các dòng sau trong AdsInitializer.kt:
// import com.ironsrc.mediationsdk.IronSource
// import com.ironsrc.mediationsdk.IronSource.AD_UNIT

// Trong method initializeIronSourceIfNeeded():
// IronSource.setAdaptersDebug(true)
// IronSource.shouldTrackNetworkState(context, true)
// IronSource.init(context, appKey, IronSource.AD_UNIT.BANNER, IronSource.AD_UNIT.INTERSTITIAL)
```

## ✅ **Verification**

Sau khi cấu hình, kiểm tra logs để đảm bảo ironSource được khởi tạo:

```
[BaseAds] Initializer: Initializing ironSource with app key: 12345678...
[BaseAds] Initializer: ironSource initialization completed
```

## 🚨 **Lưu ý bảo mật**

- ❌ **KHÔNG** hardcode app key trực tiếp trong code
- ✅ **SỬ DỤNG** BuildConfig hoặc local.properties
- ✅ **KHÁC BIỆT** app key giữa debug và production
- ✅ **KHÔNG COMMIT** local.properties lên git

## 📞 **Troubleshooting**

### Nếu ironSource không khởi tạo:
1. Kiểm tra app key có đúng format không
2. Verify dependencies đã được add
3. Check internet connection
4. Xem logs để tìm error cụ thể

### Nếu ads không show:
1. Kiểm tra ironSource dashboard setup
2. Verify mediation configuration
3. Check test device setup
4. Enable debug logging để troubleshoot