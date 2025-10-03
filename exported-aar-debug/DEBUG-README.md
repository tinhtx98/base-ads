# BaseAds Library - DEBUG VERSION

**⚠️ DEBUG BUILD - NOT FOR PRODUCTION ⚠️**

**Version:** 1.0.0  
**Version Code:**   
**Built:** Thu Oct  2 21:18:39 +07 2025  
**File:** base-ads-debug-v1.0.0-20251002_211827.aar  
**Size:** 156K  
**Type:** DEBUG with full logging

## 🐛 Debug Features

- ✅ **Full logging enabled** - All AdsLogger.d(), .i(), .w(), .e() messages
- ✅ **Debug symbols included** - Better crash reports
- ✅ **No code obfuscation** - Easier debugging
- ✅ **Source line numbers** - Accurate stack traces
- ✅ **All analytics events logged** - See every interaction

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
- 🐛 **FULL DEBUG LOGGING**

## 🚀 Integration Guide

### 1. Copy DEBUG AAR to your project
```
YourProject/
├── app/
│   ├── libs/
│   │   └── base-ads-debug-v1.0.0-20251002_211827.aar
│   └── build.gradle.kts
```

### 2. Add dependencies to app/build.gradle.kts
```kotlin
dependencies {
    // BaseAds DEBUG Library
    implementation(files("libs/base-ads-debug-v1.0.0-20251002_211827.aar"))
    
    // Required dependencies
    implementation("com.google.android.gms:play-services-ads:22.5.0")
    implementation("com.ironsource.sdk:mediationsdk:7.5.2")
    implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
    implementation("com.google.firebase:firebase-config-ktx:21.6.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
}
```

### 3. Enable logging in your app
```kotlin
// In your Application class
class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable debug logging
        AdsLogger.setLogLevel(AdsLogger.LogLevel.DEBUG)
    }
}
```

## 🐛 Debug Logging

This DEBUG version will show detailed logs for:

### Banner Ads
```
D/BaseAds-Banner: Preloading banner ad...
D/BaseAds-Banner: Banner ad loaded successfully
D/BaseAds-Banner: Showing preloaded banner
```

### Interstitial Ads  
```
D/BaseAds-Interstitial: Loading interstitial ad...
D/BaseAds-Interstitial: maybeShow called - ready: true
D/BaseAds-Interstitial: Showing interstitial ad
```

### Smart Clickable Events
```
D/BaseAds-Click: Smart click triggered: save_button
D/BaseAds-Click: Click action completed: save_button
D/BaseAds-Button: Button clicked: Save Settings
D/BaseAds-Button: Post-button-click interstitial attempt - shown: true
```

### Search Analytics
```
D/BaseAds-Search: Search triggered: query='test query'
D/BaseAds-Keyboard: Search triggered via keyboard: query='test query'
D/BaseAds-Search: Search completed successfully
```

### Analytics Events
```
D/BaseAds-Analytics: Event logged: button_click
D/BaseAds-Analytics: Event logged: search_performed
D/BaseAds-Analytics: Event logged: click_ad_attempt
```

## 🔍 Debugging Tips

1. **Filter Logcat by "BaseAds"** to see all library logs
2. **Check ads loading status** - look for "ready: true/false"
3. **Verify config values** - showInterstitialBeforeNavigate, etc.
4. **Monitor analytics events** - ensure tracking is working
5. **Watch for error messages** - all exceptions are logged

## 📱 Testing Checklist

- [ ] Banner ads show immediately after app start
- [ ] Interstitial ads appear after button clicks  
- [ ] Search analytics are logged properly
- [ ] VIP status affects ad display
- [ ] Error handling works correctly
- [ ] Analytics events are fired

## ⚠️ Important Notes

- **DO NOT use in production** - this has debug overhead
- **Large file size** - includes debug symbols
- **Verbose logging** - may impact performance
- **Use release AAR** for production builds

---

**For production:** Use `./build-aar.sh` instead  
**For debugging:** This file is perfect! 🐛
