# Primary Project Updates - InMobi Integration

## 📋 Changes Made to Primary Project Templates

Các file template trong `primary_project/` đã được cập nhật để hỗ trợ InMobi bidding adapter.

---

## 1. ✅ libs.versions.toml.tmp

### Added Version

```toml
[versions]
inmobi = "10.8.7.0"  # ← NEW!
```

### Added Library

```toml
[libraries]
inmobi-adapter = { module = "com.google.ads.mediation:inmobi", version.ref = "inmobi" }
```

**Location**: Line ~90, sau `ironsource-adapter` definition

---

## 2. ✅ build.gradle.kts.tmp

### Added Dependency

```kotlin
dependencies {
    // BaseAds Library
    implementation(files("libs/base-ads.aar"))
    
    // CRITICAL: Phải khai báo MANUAL tất cả mediation adapters
    implementation(libs.play.services.ads)
    implementation(libs.vungle)
    implementation(libs.ironsource.adapter)
    implementation(libs.facebook)
    implementation(libs.inmobi.adapter)  // ← NEW!
}
```

**Note**: Mặc dù AAR đã export InMobi qua `api` scope, nhưng vẫn nên khai báo explicit để:
- Có version control rõ ràng
- Dễ troubleshoot nếu có vấn đề
- IDE có thể suggest & autocomplete

---

## 3. ✅ proguard.pro.tmp

### Added Rules (Section 1)

```proguard
# Keep InMobi SDK classes (Basic)
-keep class com.inmobi.** { *; }
-keepclassmembers class com.inmobi.** { *; }
-dontwarn com.inmobi.**
-keep class com.inmobi.ads.** { *; }
-keep interface com.inmobi.ads.** { *; }
```

**Location**: Lines ~35-40, sau Meta Audience Network rules

### Added Rules (Section 2)

```proguard
# ========================================
# ENHANCED: InMobi Adapter Rules
# ========================================

# Keep InMobi mediation adapter package
-keep class com.google.ads.mediation.inmobi.** { *; }
-keep interface com.google.ads.mediation.inmobi.** { *; }

# Keep InMobi SDK internal classes
-keep class com.inmobi.media.** { *; }
-dontwarn com.inmobi.media.**
```

**Location**: Lines ~365-376, sau Vungle adapter rules

---

## 📊 Summary of All Mediation Adapters

Primary project giờ khai báo **5 mediation adapters**:

| # | Partner | Dependency | Version |
|---|---------|-----------|---------|
| 1 | AdMob | `play-services-ads` | 24.7.0 |
| 2 | Vungle | `libs.vungle` | 7.6.0.0 |
| 3 | IronSource | `libs.ironsource.adapter` | 9.0.0.1 |
| 4 | Meta | `libs.facebook` | 6.20.0.2 |
| 5 | **InMobi** | **`libs.inmobi.adapter`** | **10.8.7.0** ← NEW! |

---

## 🚀 How to Apply to Your Project

### Step 1: Update libs.versions.toml

```bash
# Copy from template or manually add:
```

```toml
inmobi = "10.8.7.0"

# In [libraries] section:
inmobi-adapter = { module = "com.google.ads.mediation:inmobi", version.ref = "inmobi" }
```

### Step 2: Update build.gradle.kts

```kotlin
dependencies {
    implementation(files("libs/base-ads.aar"))  // Latest AAR với InMobi
    
    // Add this line:
    implementation(libs.inmobi.adapter)
}
```

### Step 3: Update proguard-rules.pro

```bash
# Copy InMobi rules từ primary_project/proguard.pro.tmp
# Hoặc trust consumer-rules.pro từ AAR (đã bao gồm đầy đủ)
```

**Recommended**: Để AAR's consumer-rules tự động apply, nhưng thêm vào project rules để redundancy:

```proguard
# InMobi Mediation Adapter
-keep class com.google.ads.mediation.inmobi.** { *; }
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
```

### Step 4: Sync & Clean Build

```bash
# In Android Studio:
# File → Sync Project with Gradle Files

# Then clean build:
./gradlew clean
./gradlew assembleRelease
```

---

## ✅ Verification Steps

### 1. Check Gradle Sync

```bash
# Should see no errors about inmobi dependency
```

### 2. Check Build Logs

```bash
./gradlew :app:dependencies | grep inmobi

# Should see:
# +--- com.google.ads.mediation:inmobi:10.8.7.0
#      \--- com.inmobi.monetization:inmobi-ads:10.x.x (transitive)
```

### 3. Run Debug Build

```bash
adb logcat | grep -E "(InMobi|Mediation)"

# Expected output:
# ✅ com.google.ads.mediation.inmobi.InMobiMediationAdapter: READY (500ms)
```

### 4. Check Release Build

```bash
./gradlew assembleRelease

# Verify ProGuard didn't strip InMobi:
cat app/build/outputs/mapping/release/configuration.txt | grep inmobi

# Should see InMobi classes preserved
```

---

## ⚠️ Important Notes

### 1. AAR Auto-Export vs Manual Declaration

**AAR exports InMobi qua `api` scope**, nhưng:

✅ **Recommended**: Vẫn khai báo explicit trong primary project
- Dễ track versions
- Clear dependencies
- IDE support tốt hơn

❌ **Not recommended**: Rely hoàn toàn vào AAR export
- Khó troubleshoot
- Version conflicts unclear
- IDE warnings

### 2. ProGuard Rules Priority

```
1. app/proguard-rules.pro (highest priority)
2. AAR consumer-rules.pro (auto-applied)
3. library proguard-rules.pro (only during library build)
```

**Best Practice**: Khai báo ở cả 2 chỗ (app + consumer-rules) để double protection.

### 3. Version Compatibility

Đảm bảo versions compatible:

```
AdMob SDK: 24.7.0
InMobi Adapter: 10.8.7.0 (for AdMob 24.x)
InMobi SDK: 10.x.x (transitive from adapter)
```

Check compatibility: https://developers.google.com/admob/android/mediation/inmobi

---

## 🎯 Expected Results

Sau khi apply changes:

1. ✅ Gradle sync thành công
2. ✅ Build thành công (debug & release)
3. ✅ InMobi adapter loads: "READY" in logs
4. ✅ eCPM tracking works (not "-")
5. ✅ No ProGuard errors in release

---

## 🆘 Troubleshooting

### Issue: "Could not resolve libs.inmobi.adapter"

**Solution**:
```bash
# Check libs.versions.toml có đúng syntax không
# Sync project again
# Invalidate Caches & Restart
```

### Issue: "Duplicate class com.inmobi.*"

**Solution**:
```kotlin
// Nếu có conflict với standalone InMobi SDK, exclude nó:
implementation(libs.inmobi.adapter) {
    exclude(group = "com.inmobi.monetization", module = "inmobi-ads")
}
```

### Issue: InMobi adapter not loading

**Solution**:
```bash
# Verify AAR mới nhất đã copy
# Check logs cho errors
adb logcat | grep -A10 "InMobi"

# Verify ProGuard rules
cat app/build/outputs/mapping/release/configuration.txt | grep -C5 "inmobi"
```

---

## 📚 Related Documentation

- [INMOBI_SETUP_GUIDE.md](../INMOBI_SETUP_GUIDE.md) - Full InMobi setup
- [MEDIATION_PARTNERS_SUMMARY.md](../MEDIATION_PARTNERS_SUMMARY.md) - All partners overview
- [PRIMARY_PROJECT_INTEGRATION.md](../PRIMARY_PROJECT_INTEGRATION.md) - Quick integration guide

---

**Last Updated**: November 13, 2025  
**InMobi Adapter Version**: 10.8.7.0  
**Status**: ✅ Templates Updated & Verified
