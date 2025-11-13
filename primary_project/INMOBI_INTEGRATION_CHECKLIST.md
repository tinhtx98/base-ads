# InMobi Integration Checklist for Primary Project

## ✅ Cập nhật Files (3 files)

### 1. libs.versions.toml
```toml
[versions]
inmobi = "10.8.7.0"

[libraries]
inmobi-adapter = { module = "com.google.ads.mediation:inmobi", version.ref = "inmobi" }
```
- [ ] Đã thêm version `inmobi`
- [ ] Đã thêm library `inmobi-adapter`

---

### 2. build.gradle.kts (Module: app)
```kotlin
dependencies {
    implementation(files("libs/base-ads.aar"))
    
    // Mediation adapters
    implementation(libs.play.services.ads)
    implementation(libs.vungle)
    implementation(libs.ironsource.adapter)
    implementation(libs.facebook)
    implementation(libs.inmobi.adapter)  // ← Thêm dòng này
}
```
- [ ] Đã copy AAR mới nhất vào `app/libs/base-ads.aar`
- [ ] Đã thêm `implementation(libs.inmobi.adapter)`

---

### 3. proguard-rules.pro (Optional nhưng recommended)
```proguard
# InMobi Mediation Adapter
-keep class com.google.ads.mediation.inmobi.** { *; }
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
-keep class com.inmobi.ads.** { *; }
-keep interface com.inmobi.ads.** { *; }
-keep class com.inmobi.media.** { *; }
-dontwarn com.inmobi.media.**
```
- [ ] Đã thêm InMobi ProGuard rules (hoặc trust consumer-rules từ AAR)

---

## 🔨 Build & Test

### Gradle Sync
- [ ] Sync Project with Gradle Files (no errors)

### Clean Build
- [ ] `./gradlew clean` thành công
- [ ] `./gradlew assembleDebug` thành công
- [ ] `./gradlew assembleRelease` thành công

### Dependency Verification
```bash
./gradlew :app:dependencies | grep inmobi
```
- [ ] Thấy `com.google.ads.mediation:inmobi:10.8.7.0`
- [ ] Thấy transitive dependency `com.inmobi.monetization:inmobi-ads`

---

## 🧪 Runtime Testing

### Debug Build
```bash
adb logcat | grep -E "(Mediation|InMobi)"
```
- [ ] Thấy log: `InMobiMediationAdapter: READY`
- [ ] Không có errors về InMobi

### Load Test Ad
- [ ] Banner ad loads successfully
- [ ] Interstitial ad loads successfully
- [ ] Không crash

### Check Adapter Status
```bash
adb logcat | grep "Adapter"
```
Expected:
```
✅ VungleMediationAdapter: READY
✅ IronSourceMediationAdapter: READY
✅ FacebookMediationAdapter: READY
✅ InMobiMediationAdapter: READY  ← Must see this!
```
- [ ] InMobi adapter status = READY

---

## 🏭 Release Build Verification

### ProGuard Check
```bash
cat app/build/outputs/mapping/release/configuration.txt | grep -i inmobi
```
- [ ] InMobi classes được keep (not stripped)
- [ ] Thấy rules cho `com.google.ads.mediation.inmobi`
- [ ] Thấy rules cho `com.inmobi.**`

### Release APK Test
- [ ] Install release APK
- [ ] Test ads loading
- [ ] No crashes related to InMobi

---

## 🎯 AdMob Console Setup

### Create/Update Mediation Group
- [ ] Vào AdMob Console → Mediation
- [ ] Select existing mediation group hoặc create new
- [ ] Click "Add ad sources"

### Add InMobi
- [ ] Chọn **InMobi** từ list
- [ ] Select **"Set up bidding"** (QUAN TRỌNG!)
- [ ] Nhập **InMobi Placement ID** từ InMobi dashboard

### Verify Configuration
```
Mediation Group: [Your Ad Unit Name]
├── Ad Unit ID: ca-app-pub-xxxxx/yyyyyyy
└── Ad Sources (Bidding):
    ├── AdMob Network (always enabled)
    ├── Meta Audience Network
    ├── IronSource
    ├── Vungle/LiftOff
    └── InMobi ← Must see this!
```
- [ ] InMobi appears in ad sources list
- [ ] Status = "Active" (có thể mất vài phút)

---

## 📊 Performance Monitoring

### First 24 Hours
- [ ] Check AdMob Console → Mediation → Reports
- [ ] Verify InMobi shows impressions (có thể ít lúc đầu)
- [ ] Check eCPM value != "-" (sau vài test impressions)

### After 48 Hours
- [ ] InMobi fill rate stabilized
- [ ] Compare eCPM: Before vs After InMobi
- [ ] Verify no increase in crashes/errors

---

## ✅ Success Criteria

Tất cả must be TRUE:

- [x] Gradle sync & build thành công
- [x] 5/5 adapters load successfully (debug logs)
- [x] InMobi shows eCPM in AdMob Console (not "-")
- [x] No ProGuard errors in release build
- [x] No crashes related to InMobi
- [x] Revenue increase ~5-10% (từ thêm 1 bidder)

---

## 🆘 If Something Fails

### Gradle Sync Error
→ Check [INMOBI_UPDATE_SUMMARY.md](INMOBI_UPDATE_SUMMARY.md) Section "Troubleshooting"

### Adapter Not Loading
→ Check [INMOBI_SETUP_GUIDE.md](../INMOBI_SETUP_GUIDE.md) Section "Debugging"

### eCPM = "-"
→ Check [PRIMARY_PROJECT_INTEGRATION.md](../PRIMARY_PROJECT_INTEGRATION.md) Section "Troubleshooting"

### ProGuard Stripping InMobi
→ Verify AAR is latest version (Nov 13, 2025+)
→ Check consumer-rules.pro có trong AAR không

---

## 📞 Quick Reference

| Item | Value |
|------|-------|
| InMobi Adapter Version | 10.8.7.0 |
| AdMob SDK Required | 24.7.0+ |
| BaseAds AAR | `exported-aar/base-ads.aar` |
| Consumer Rules | Auto-applied from AAR |
| Documentation | `INMOBI_SETUP_GUIDE.md` |

---

**Checklist Version**: 1.0  
**Last Updated**: November 13, 2025  
**Status**: Ready to use
