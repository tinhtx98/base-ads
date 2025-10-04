# BaseAds Library

**Version:** 1.0.0  
**Version Code:**   
**Built:** Fri Oct  3 19:38:36 +07 2025  
**File:** base-ads-v1.0.0-20251003_193831.aar  
**Size:** 164K

## 📦 What's Included

- ✅ Google Mobile Ads SDK integration
- ✅ ironSource mediation support
- ✅ Firebase Analytics integration
- ✅ Adaptive banner ads with preloading
- ✅ Interstitial ads with smart timing
- ✅ VIP user management
- ✅ Force update functionality
- ✅ Jetpack Compose UI components
- ✅ Hilt dependency injection ready

## 🚀 Integration Guide

### 1. Copy AAR to your project
```
YourProject/
├── app/
│   ├── libs/
│   │   └── base-ads-v1.0.0-20251003_193831.aar
│   └── build.gradle.kts
```

### 2. Add dependencies to app/build.gradle.kts
```kotlin
dependencies {
    // BaseAds Library
    implementation(files("libs/base-ads-v1.0.0-20251003_193831.aar"))
    
    // Required dependencies
    implementation("com.google.android.gms:play-services-ads:22.5.0")
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    implementation("com.google.firebase:firebase-config:21.4.1")
    implementation("com.ironsource.sdk:mediationsdk:7.5.1")
    
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.4")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

### 3. Enable Jetpack Compose in android block
```kotlin
android {
    compileSdk 34
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}
```

### 4. Setup Application class
```kotlin
import com.tinhtx.baseads.core.AdsInitializer
import com.tinhtx.baseads.data.AdsPrefs
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject
    lateinit var adsInitializer: AdsInitializer
    
    @Inject 
    lateinit var adsPrefs: AdsPrefs
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ads
        adsPrefs.incrementAppLaunchCount()
        adsInitializer.initializeWithTestDevices(
            context = this,
            includeCommonTestDevices = true
        )
    }
}
```

### 5. Add to AndroidManifest.xml
```xml
<application
    android:name=".MyApplication"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/Theme.YourApp">
    
    <!-- Google AdMob App ID -->
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-3940256099942544~3347511713" />
    
    <!-- Your activities here -->
    
</application>
```

### 6. Configure ads (Create AdsModule.kt)
```kotlin
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdUnitsProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MyAppAdsModule {
    
    @Provides
    @Singleton
    fun provideAdsConfig(): AdsConfig = AdsConfig(
        enableAds = true,
        enableInterstitial = true,
        enableBanner = true,
        ironSourceAppKey = "YOUR_IRONSOURCE_APP_KEY", // Replace with your key
        enableIronSourceLogging = BuildConfig.DEBUG
    )
    
    @Provides
    @Singleton 
    fun provideAdUnitsProvider(): AdUnitsProvider = object : AdUnitsProvider {
        override val bannerAdUnitId: String = "ca-app-pub-3940256099942544/6300978111" // Test ID
        override val interstitialAdUnitId: String = "ca-app-pub-3940256099942544/1033173712" // Test ID
        override val rewardedAdUnitId: String = "ca-app-pub-3940256099942544/5224354917" // Test ID
    }
}
```

### 7. Use banner ads in Compose
```kotlin
import com.tinhtx.baseads.banner.AdaptiveBanner
import com.tinhtx.baseads.core.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainScreen() {
    val adUnitsProvider: AdUnitsProvider = hiltViewModel()
    val adsConfig: AdsConfig = hiltViewModel()
    val vipGate: VipGate = hiltViewModel()
    val analyticsLogger: AnalyticsLogger = hiltViewModel()
    
    Scaffold(
        bottomBar = {
            AdaptiveBanner(
                adUnitsProvider = adUnitsProvider,
                adsConfig = adsConfig,
                vipGate = vipGate,
                analyticsLogger = analyticsLogger
            )
        }
    ) { paddingValues ->
        // Your content here
    }
}
```

## 🔧 Configuration

### Ad Unit IDs
Replace test ad unit IDs with your production IDs:
- Banner: `ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX`
- Interstitial: `ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX`

### ironSource Integration
1. Get your app key from ironSource dashboard
2. Replace `YOUR_IRONSOURCE_APP_KEY` in AdsModule
3. Add ironSource adapters as needed

### Firebase Setup
1. Add `google-services.json` to app folder
2. Add Firebase plugin to app/build.gradle.kts:
   ```kotlin
   plugins {
       id("com.google.gms.google-services")
   }
   ```

## 📚 API Reference

### Key Classes
- `AdsInitializer` - Initialize ads SDK
- `AdaptiveBanner` - Compose banner component  
- `InterstitialAdManager` - Manage interstitial ads
- `VipGate` - VIP user management
- `ForceUpdateDialog` - App update enforcement

### Features
- ✅ Adaptive banners with preloading
- ✅ Smart interstitial timing
- ✅ VIP user ad-free experience
- ✅ Force update with Firebase Remote Config
- ✅ Analytics integration
- ✅ ironSource mediation support

## 🆘 Support

For issues and questions:
- Check logs with tag: `BaseAds-*`
- Enable debug logging in AdsConfig
- Test with provided test ad unit IDs first

---
**Generated:** Fri Oct  3 19:38:36 +07 2025  
**BaseAds Library v1.0.0**
