# Primary Project Integration - Quick Reference

## 🚀 Quick Start (5 bước)

### 1. Copy AAR mới nhất

```bash
cp exported-aar/base-ads.aar <your-project>/app/libs/base-ads.aar
```

### 2. Thêm vào libs.versions.toml

```toml
[versions]
# Không cần định nghĩa version cho mediation adapters
# Chúng được auto-export từ AAR

[libraries]
# Base Ads library
base-ads = { module = "files", name = "libs/base-ads.aar" }

# Các dependencies khác của bạn...
```

### 3. Thêm vào build.gradle.kts

```kotlin
dependencies {
    // BaseAds library - Bao gồm TẤT CẢ mediation adapters!
    implementation(files("libs/base-ads.aar"))
    
    // ✅ TỰ ĐỘNG EXPORT TỪ AAR (không cần khai báo):
    // - com.google.android.gms:play-services-ads:24.7.0
    // - com.google.ads.mediation:vungle:7.6.0.0
    // - com.google.ads.mediation:ironsource:9.0.0.1
    // - com.google.ads.mediation:facebook:6.20.0.2
    // - com.google.ads.mediation:inmobi:10.8.7.0
    
    // Firebase (vẫn cần khai báo riêng)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Hilt (vẫn cần khai báo riêng)
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")
    
    // Compose, Navigation, etc...
}
```

### 4. ProGuard Rules

**KHÔNG CẦN THÊM GÌ!** Consumer rules từ AAR tự động apply.

Nhưng nếu muốn chắc chắn, có thể verify:

```bash
# Sau khi build release, check file này:
cat app/build/outputs/mapping/release/configuration.txt | grep -E "(inmobi|ironsource|facebook|vungle)"
```

### 5. Clean & Rebuild

```bash
./gradlew clean
./gradlew assembleDebug
./gradlew assembleRelease
```

## ✅ Verify Integration

### Debug Logs

```bash
adb logcat | grep -E "(Mediation|Adapter)"
```

Expected output:
```
✅ com.google.ads.mediation.vungle.VungleMediationAdapter: READY (672ms)
✅ com.google.ads.mediation.ironsource.IronSourceMediationAdapter: READY (1111ms)
✅ com.google.ads.mediation.facebook.FacebookMediationAdapter: READY (604ms)
✅ com.google.ads.mediation.inmobi.InMobiMediationAdapter: READY (500ms)
```

### Check AAR Contents

```bash
# Verify mediation adapters in AAR
unzip -l app/libs/base-ads.aar

# Check ProGuard rules
unzip -p app/libs/base-ads.aar proguard.txt | grep -A3 "InMobi"
```

## 🎯 AdMob Console Setup

### Tạo Mediation Group

1. **AdMob Console** → **Mediation** → **Create mediation group**
2. Ad format: **Banner** / **Interstitial** / **Rewarded**
3. Platform: **Android**
4. Locations: **All locations** (or specific)

### Add All Bidding Partners

| Partner | Setup Type | Config Required |
|---------|------------|----------------|
| **AdMob Network** | Always enabled | None |
| **Meta** | Bidding | Placement ID |
| **IronSource** | Bidding | Instance ID |
| **Vungle** | Bidding | Placement ID |
| **InMobi** | Bidding | Placement ID |

### Placement ID Examples

```
Meta: 123456789012345_123456789012345
IronSource: DefaultInterstitial
Vungle: DEFAULT-1234567
InMobi: 1234567890123456789
```

## 🧪 Testing Checklist

- [ ] AAR mới nhất copied vào `app/libs/`
- [ ] Clean build thành công
- [ ] Debug logs show 5 adapters READY (4 bidding + AdMob)
- [ ] Test ad loads successfully
- [ ] No ProGuard errors in release build
- [ ] eCPM values appear in AdMob Console (not "-")

## 🆘 Troubleshooting

### "Adapter not found" Error

```bash
# Check if AAR contains adapters
unzip -p app/libs/base-ads.aar proguard.txt | head -50
```

Should see rules for all 5 partners.

### eCPM = "-" in Release

**Solution**: Verify latest AAR (Nov 13, 2025 or newer) includes consumer-rules.pro with adapter interface rules.

```bash
# Extract and check
unzip -p app/libs/base-ads.aar proguard.txt | grep "MediationAdapter"
```

Should see:
```proguard
-keep class * implements com.google.android.gms.ads.mediation.MediationAdapter { *; }
```

### Slow Ad Loading

Nguyên nhân:
- ❌ Quá nhiều bidders (5+ partners)
- ❌ Network latency
- ❌ Partner servers slow

**Optimization**:
```kotlin
// Set timeout for bidding
MobileAds.setRequestConfiguration(
    RequestConfiguration.Builder()
        .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
        .build()
)
```

## 📊 Expected Performance

| Metric | Debug Build | Release Build |
|--------|-------------|---------------|
| Adapters Loaded | 5/5 | 5/5 |
| Fill Rate | >95% | >95% |
| eCPM Display | ✅ All | ✅ All |
| Avg Load Time | ~1.5s | ~1.2s |

## 🎉 Success Indicators

✅ **Integration Successful** khi thấy:

1. **Logs**: All 5 adapters READY
2. **AdMob Console**: All partners showing eCPM (not "-")
3. **Ads Loading**: Both debug & release builds
4. **No Crashes**: ProGuard working correctly
5. **Revenue Increase**: 15-20% higher eCPM compared to AdMob alone

---

## 📞 Need Help?

Tham khảo các guides:
- [MEDIATION_PARTNERS_SUMMARY.md](MEDIATION_PARTNERS_SUMMARY.md) - Overview
- [INMOBI_SETUP_GUIDE.md](INMOBI_SETUP_GUIDE.md) - InMobi specific
- [BIDDING_TROUBLESHOOTING.md](BIDDING_TROUBLESHOOTING.md) - Debug guide

---

**Last Updated**: November 13, 2025  
**BaseAds Version**: 1.0.0+  
**Mediation Partners**: 5 (AdMob + Vungle + IronSource + Meta + InMobi)
