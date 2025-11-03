# BaseAds AAR Integration Guide

## 📦 Latest Version: v1.0.0-20251103_141146

✅ **New**: Fixed ProGuard issues with Meta Audience Network SDK

### What's Included
- ✅ Google AdMob mediation support
- ✅ ironSource bidding adapter
- ✅ Meta Audience Network (Facebook) adapter
- ✅ Vungle adapter
- ✅ Complete ProGuard/R8 rules
- ✅ Firebase Analytics integration

---

## 🚀 Quick Integration

### Step 1: Copy Files
```
your-project/
└── app/
    └── libs/
        ├── base-ads-v1.0.0-20251103_141146.aar
        └── base-ads-proguard-rules.pro  (optional but recommended)
```

### Step 2: Update build.gradle

**app/build.gradle.kts:**
```kotlin
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "base-ads-proguard-rules.pro"  // Optional: for explicit ProGuard rules
            )
        }
    }
}

dependencies {
    // BaseAds Library
    implementation(files("libs/base-ads-v1.0.0-20251103_141146.aar"))
    
    // REQUIRED: Mediation SDK Dependencies
    implementation("com.google.android.gms:play-services-ads:23.4.0")
    implementation("com.ironsource.sdk:mediationsdk:8.3.0")
    implementation("com.facebook.android:audience-network-sdk:6.17.0")
    implementation("com.vungle:vungle-ads:7.4.0")
    
    // REQUIRED: Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Optional: Hilt (if using dependency injection)
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
}
```

### Step 3: Add google-services.json
Place your Firebase configuration file in `app/google-services.json`

### Step 4: Initialize in Application class
```kotlin
import com.tinhtx.baseads.BaseAdsSDK
import com.google.android.gms.ads.MobileAds

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) { status ->
            Log.d("BaseAds", "AdMob initialized: $status")
        }
        
        // Initialize BaseAds (if using)
        BaseAdsSDK.initialize(this)
    }
}
```

---

## 🔧 ProGuard Configuration

### Option 1: Automatic (Recommended)
The AAR includes `consumer-rules.pro` which will be automatically applied when you import the AAR. **No additional configuration needed for most cases.**

### Option 2: Manual Rules
If you encounter ProGuard issues, copy `proguard-rules.pro` to your app folder and reference it:

```kotlin
android {
    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "base-ads-proguard-rules.pro"
            )
        }
    }
}
```

### Important ProGuard Rules (Already Included)
```proguard
# Meta Audience Network - CRITICAL for production builds
-keep class com.facebook.ads.** { *; }
-dontwarn com.facebook.infer.annotation.**
-keep class com.facebook.infer.annotation.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }

# ironSource
-keep class com.ironsource.** { *; }

# Vungle
-keep class com.vungle.** { *; }
```

---

## 📱 Usage Examples

### Banner Ad
```kotlin
import com.tinhtx.baseads.banner.BannerAdView

// In your layout XML
<com.tinhtx.baseads.banner.BannerAdView
    android:id="@+id/bannerAd"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:adUnitId="ca-app-pub-xxxxx/xxxxx" />

// In your Activity/Fragment
val bannerAd = findViewById<BannerAdView>(R.id.bannerAd)
bannerAd.loadAd()
```

### Interstitial Ad
```kotlin
import com.tinhtx.baseads.interstitial.InterstitialAdManager

class MainActivity : AppCompatActivity() {
    private lateinit var interstitialManager: InterstitialAdManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        interstitialManager = InterstitialAdManager(
            context = this,
            adUnitId = "ca-app-pub-xxxxx/xxxxx"
        )
        
        // Load ad
        interstitialManager.loadAd()
    }
    
    private fun showInterstitial() {
        interstitialManager.showAd { success ->
            if (success) {
                Log.d("Ads", "Interstitial shown")
            } else {
                Log.d("Ads", "Interstitial not ready")
            }
        }
    }
}
```

### Rewarded Ad
```kotlin
import com.tinhtx.baseads.rewarded.RewardedAdManager

class GameActivity : AppCompatActivity() {
    private lateinit var rewardedManager: RewardedAdManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        rewardedManager = RewardedAdManager(
            context = this,
            adUnitId = "ca-app-pub-xxxxx/xxxxx"
        )
        
        rewardedManager.loadAd()
    }
    
    private fun showRewardedAd() {
        rewardedManager.showAd { reward ->
            if (reward != null) {
                // User earned reward
                val amount = reward.amount
                val type = reward.type
                Log.d("Ads", "Earned $amount $type")
            }
        }
    }
}
```

---

## ✅ Testing Checklist

### Development Testing
- [ ] Add test device IDs in AdMob dashboard
- [ ] Test banner ads load and display
- [ ] Test interstitial ads load and show
- [ ] Test rewarded ads and reward callback
- [ ] Check logs for mediation waterfall
- [ ] Verify Meta adapter loads

### Production Build Testing
- [ ] Build release APK with `minifyEnabled = true`
- [ ] Verify no ProGuard warnings about Meta SDK
- [ ] Install on real device (not emulator)
- [ ] Test all ad formats in release build
- [ ] Check Firebase Analytics events
- [ ] Verify ILRD (impression-level revenue data)

---

## 🐛 Troubleshooting

### ProGuard Errors (Meta SDK)
**Error:** `Missing class com.facebook.infer.annotation.Nullsafe`

**Solution:** Rules are already included in `consumer-rules.pro`. If still getting errors:
1. Copy `proguard-rules.pro` to your app folder
2. Reference it in `build.gradle` (see ProGuard Configuration above)
3. Clean and rebuild: `./gradlew clean assembleRelease`

### Ads Not Showing
1. **Check Ad Unit IDs** - Ensure using correct production IDs
2. **Test Device** - Add test device ID during development
3. **Internet Permission** - Check `AndroidManifest.xml` has internet permission
4. **Firebase Config** - Verify `google-services.json` is correct
5. **Mediation Setup** - Check AdMob dashboard for adapter status

### Build Errors
1. **Missing Dependencies** - Ensure all mediation SDKs are added
2. **Version Conflicts** - Use BOM for Firebase dependencies
3. **MultiDex** - Enable if hitting 64K method limit

---

## 📚 Additional Documentation

- `CHANGELOG.md` - Version history and changes
- `PROGUARD_META_FIX.md` - Detailed ProGuard troubleshooting guide
- `proguard-rules.pro` - Complete ProGuard rules reference
- `consumer-rules.pro` - Auto-applied consumer ProGuard rules

---

## 🔗 Required Ad Unit IDs

Before going to production, replace test IDs with your own:

### AdMob Dashboard
1. Create ad units for each format:
   - Banner: `ca-app-pub-xxxxx/xxxxx`
   - Interstitial: `ca-app-pub-xxxxx/xxxxx`
   - Rewarded: `ca-app-pub-xxxxx/xxxxx`

2. Set up mediation groups with:
   - ironSource adapter
   - Meta Audience Network adapter
   - Vungle adapter

3. Enable bidding for optimal eCPM

---

## 📞 Support

For issues or questions:
1. Check `PROGUARD_META_FIX.md` for ProGuard issues
2. Review `CHANGELOG.md` for recent changes
3. Verify all dependencies are up to date

---

## 📄 License

Copyright © 2025. All rights reserved.
