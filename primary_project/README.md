# Primary Project Configuration Templates

## 📁 Folder Contents

Template files cho primary project sử dụng BaseAds library.

```
primary_project/
├── README.md                              ← This file
├── libs.versions.toml.tmp                 ← Gradle version catalog
├── build.gradle.kts.tmp                   ← App module build config
├── proguard.pro.tmp                       ← ProGuard rules
├── INMOBI_UPDATE_SUMMARY.md              ← InMobi integration summary
└── INMOBI_INTEGRATION_CHECKLIST.md       ← Step-by-step checklist
```

---

## 🎯 Purpose

Các file này là **templates/examples** cho primary project integrate BaseAds AAR với **5 bidding mediation partners**:

1. ✅ **AdMob** (Primary platform)
2. ✅ **Vungle/LiftOff** - Bidding
3. ✅ **IronSource** - Bidding  
4. ✅ **Meta Audience Network** - Bidding
5. ✅ **InMobi** - Bidding

---

## 📋 File Descriptions

### 1. libs.versions.toml.tmp

**Purpose**: Gradle version catalog định nghĩa tất cả dependencies versions

**Key sections**:
```toml
[versions]
playServicesAds = "24.7.0"
vungle = "7.6.0.0"
ironSourceAdapter = "9.0.0.1"
facebook = "6.20.0.2"
inmobi = "10.8.7.0"

[libraries]
play-services-ads = { ... }
vungle = { module = "com.google.ads.mediation:vungle", ... }
ironsource-adapter = { module = "com.google.ads.mediation:ironsource", ... }
facebook = { module = "com.google.ads.mediation:facebook", ... }
inmobi-adapter = { module = "com.google.ads.mediation:inmobi", ... }
```

**How to use**:
1. Copy relevant sections to your `gradle/libs.versions.toml`
2. Adjust versions if needed
3. Sync project

---

### 2. build.gradle.kts.tmp

**Purpose**: App module build configuration

**Key sections**:
```kotlin
dependencies {
    // BaseAds AAR
    implementation(files("libs/base-ads.aar"))
    
    // Mediation adapters (MUST declare explicitly)
    implementation(libs.play.services.ads)
    implementation(libs.vungle)
    implementation(libs.ironsource.adapter)
    implementation(libs.facebook)
    implementation(libs.inmobi.adapter)
    
    // Other dependencies...
}

buildTypes {
    release {
        buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-xxx~xxx\"")
        buildConfigField("String", "IRONSOURCE_APP_KEY", "\"xxx\"")
    }
}
```

**How to use**:
1. Copy dependencies block
2. Copy buildConfigField declarations
3. Replace with your actual IDs
4. Sync & build

---

### 3. proguard.pro.tmp

**Purpose**: ProGuard/R8 rules cho release builds

**Key sections**:
```proguard
# Mediation Adapter Interfaces (CRITICAL!)
-keep class * implements com.google.android.gms.ads.mediation.MediationAdapter { *; }
-keep class * implements com.google.android.gms.ads.mediation.Adapter { *; }
-keep class * implements com.google.android.gms.ads.mediation.rtb.RtbAdapter { *; }

# Individual adapters
-keep class com.google.ads.mediation.vungle.** { *; }
-keep class com.google.ads.mediation.ironsource.** { *; }
-keep class com.google.ads.mediation.facebook.** { *; }
-keep class com.google.ads.mediation.inmobi.** { *; }

# SDK classes
-keep class com.vungle.** { *; }
-keep class com.ironsource.** { *; }
-keep class com.facebook.ads.** { *; }
-keep class com.inmobi.** { *; }
```

**How to use**:
1. Copy relevant sections to `app/proguard-rules.pro`
2. Or trust consumer-rules.pro from AAR (auto-applied)
3. **Recommended**: Add to both for redundancy

---

### 4. INMOBI_UPDATE_SUMMARY.md

**Purpose**: Chi tiết các changes để tích hợp InMobi

**Contents**:
- Exact code changes cho 3 files
- Verification steps
- Troubleshooting guide
- Expected results

**When to read**: Khi integrate InMobi lần đầu

---

### 5. INMOBI_INTEGRATION_CHECKLIST.md

**Purpose**: Step-by-step checklist cho InMobi integration

**Contents**:
- [ ] File updates checklist
- [ ] Build verification
- [ ] Runtime testing
- [ ] AdMob Console setup
- [ ] Performance monitoring

**When to use**: Follow theo từng bước khi integrate

---

## 🚀 Quick Start Guide

### Scenario 1: New Project

```bash
# 1. Create new Android project
# 2. Copy base-ads.aar to app/libs/
cp exported-aar/base-ads.aar <your-project>/app/libs/

# 3. Copy sections from templates:
# - libs.versions.toml.tmp → your gradle/libs.versions.toml
# - build.gradle.kts.tmp → your app/build.gradle.kts
# - proguard.pro.tmp → your app/proguard-rules.pro

# 4. Sync & Build
./gradlew clean assembleRelease
```

### Scenario 2: Existing Project + Add InMobi

```bash
# Follow INMOBI_INTEGRATION_CHECKLIST.md step-by-step
```

### Scenario 3: Update BaseAds AAR

```bash
# 1. Copy new AAR
cp exported-aar/base-ads.aar <your-project>/app/libs/

# 2. Sync & rebuild
./gradlew clean assembleRelease

# 3. Test
adb logcat | grep Mediation
```

---

## ⚠️ Important Notes

### 1. Template vs Actual Files

These are `.tmp` files = **TEMPLATES/EXAMPLES**

**Do NOT**:
- Copy-paste entire files blindly
- Replace your existing configs completely

**DO**:
- Extract relevant sections
- Adapt to your project structure
- Keep your existing configs

### 2. Dependency Declaration

**AAR exports adapters via `api`**, nhưng:

✅ **Best Practice**: Declare explicitly in primary project
```kotlin
implementation(libs.inmobi.adapter)  // Explicit
```

❌ **Not Recommended**: Rely only on AAR transitive deps
```kotlin
// Nothing - depends on AAR
// ❌ Hard to track, version conflicts unclear
```

### 3. ProGuard Priority

```
Priority (High → Low):
1. app/proguard-rules.pro
2. AAR consumer-rules.pro (auto-applied)
3. library/proguard-rules.pro
```

**Recommendation**: Add rules to both #1 and #2 for safety.

---

## 📊 What's Included in Templates

| Component | Status | Notes |
|-----------|--------|-------|
| AdMob 24.7.0 | ✅ | Primary platform |
| Vungle 7.6.0.0 | ✅ | Bidding adapter |
| IronSource 9.0.0.1 | ✅ | Bidding adapter |
| Meta 6.20.0.2 | ✅ | Bidding adapter |
| InMobi 10.8.7.0 | ✅ | **NEW!** Bidding adapter |
| Firebase | ✅ | Analytics, Crashlytics, Remote Config |
| Hilt | ✅ | Dependency injection |
| Room | ✅ | Local database |
| Compose | ✅ | UI framework |
| ProGuard Rules | ✅ | All adapters protected |

---

## ✅ Validation

After applying templates, verify:

### Gradle
- [ ] Project syncs without errors
- [ ] All dependencies resolve correctly

### Build
- [ ] Debug build succeeds
- [ ] Release build succeeds
- [ ] APK size reasonable (~5-10MB with ads)

### Runtime
- [ ] 5 adapters load: Vungle, IronSource, Meta, InMobi, AdMob
- [ ] Ads display correctly
- [ ] No crashes

### AdMob Console
- [ ] All 5 partners show in mediation group
- [ ] eCPM tracking works (not "-")
- [ ] Fill rate >95%

---

## 🆘 Troubleshooting

### "Could not resolve libs.xxx"
→ Check `libs.versions.toml` syntax
→ Sync project again
→ Invalidate caches & restart

### "Duplicate class" errors
→ Check for conflicting dependencies
→ May need to exclude transitive deps

### Adapters not loading
→ Verify AAR is latest version
→ Check ProGuard rules
→ See logs: `adb logcat | grep Adapter`

### eCPM = "-" in release
→ Verify AAR from Nov 13, 2025+ (has consumer-rules fix)
→ Check ProGuard mapping file
→ See [PRIMARY_PROJECT_INTEGRATION.md](../PRIMARY_PROJECT_INTEGRATION.md)

---

## 📚 Related Documentation

| File | Purpose |
|------|---------|
| [INMOBI_SETUP_GUIDE.md](../INMOBI_SETUP_GUIDE.md) | Full InMobi setup walkthrough |
| [MEDIATION_PARTNERS_SUMMARY.md](../MEDIATION_PARTNERS_SUMMARY.md) | Overview of all 5 partners |
| [PRIMARY_PROJECT_INTEGRATION.md](../PRIMARY_PROJECT_INTEGRATION.md) | Quick start guide |
| [BIDDING_TROUBLESHOOTING.md](../BIDDING_TROUBLESHOOTING.md) | Debug common issues |

---

## 🎯 Success Metrics

After proper integration:

| Metric | Expected Value |
|--------|----------------|
| Adapters Loaded | 5/5 |
| Fill Rate | >95% |
| Avg eCPM | $0.35-0.45 |
| Revenue Increase | +15-20% vs AdMob only |
| Crash Rate | No increase |

---

**Templates Version**: 1.0  
**Last Updated**: November 13, 2025  
**Total Mediation Partners**: 5  
**Status**: ✅ Production Ready
