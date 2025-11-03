# BaseAds AAR Changelog

## v1.0.0-20251103_141146 (November 3, 2025)

### 🆕 New Features
- ✅ Added Meta Audience Network (Facebook) SDK support
- ✅ Complete ProGuard/R8 rules for all mediation partners

### 🐛 Bug Fixes
- ✅ Fixed ProGuard missing class errors for Meta SDK annotations
  - Fixed: `Missing class com.facebook.infer.annotation.Nullsafe$Mode`
  - Fixed: `Missing class com.facebook.infer.annotation.Nullsafe`

### 📝 ProGuard Rules Updates

#### Added Meta Audience Network Rules:
```proguard
# Keep Meta Audience Network (Facebook) SDK classes
-keep class com.facebook.ads.** { *; }
-keepclassmembers class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**
-dontwarn com.facebook.infer.annotation.**
-keep class com.facebook.infer.annotation.** { *; }
```

#### Updated Files:
- `base-ads/proguard-rules.pro` - Main ProGuard rules
- `base-ads/consumer-rules.pro` - Auto-applied consumer rules
- `exported-aar/proguard-rules.pro` - Distribution rules

### 📦 Package Contents
- `base-ads-v1.0.0-20251103_141146.aar` - Main library (173 KB)
- `proguard-rules.pro` - ProGuard rules for integration
- `consumer-rules.pro` - Auto-applied ProGuard rules

### 🔧 Integration

**build.gradle (app module):**
```kotlin
dependencies {
    implementation(files("libs/base-ads-v1.0.0-20251103_141146.aar"))
    
    // Required: Mediation SDKs
    implementation("com.google.android.gms:play-services-ads:23.4.0")
    implementation("com.ironsource.sdk:mediationsdk:8.3.0")
    implementation("com.facebook.android:audience-network-sdk:6.17.0")
    implementation("com.vungle:vungle-ads:7.4.0")
}
```

**Optional: Include ProGuard rules explicitly**
```kotlin
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "base-ads-proguard-rules.pro"  // Copy proguard-rules.pro and rename
            )
        }
    }
}
```

### ✅ Testing Checklist
- [x] Build success with minifyEnabled = true
- [x] No ProGuard warnings for Meta SDK
- [x] Banner ads display correctly
- [x] Interstitial ads display correctly
- [x] Rewarded ads display correctly
- [x] Meta bidding works in mediation waterfall

### 📚 Documentation
- See `PROGUARD_META_FIX.md` for detailed ProGuard troubleshooting
- See `PROGUARD_FIX_CHECKLIST.md` for integration steps

### 🔗 Related Issues
- Fixed production build errors with Meta SDK
- Improved ProGuard configuration for all mediation adapters

---

## Previous Versions

### v1.0.0-20251102_204306
- Initial release with AdMob, ironSource, Vungle support
- Basic ProGuard rules
- Known issue: Missing Meta SDK ProGuard rules
