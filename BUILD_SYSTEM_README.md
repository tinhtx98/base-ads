# BaseAds Library Build System

Hệ thống build tự động cho thư viện BaseAds, giúp export module thành file AAR để dễ dàng integrate vào các project khác.

## 🚀 Quick Start

### Build AAR hiện tại
```bash
./build-aar.sh
```

### Release version mới
```bash
./quick-release.sh
```

### Cập nhật version thủ công
```bash
./update-version.sh
```

## 📁 Output Files

Sau khi build, bạn sẽ có:

```
exported-aar/
├── base-ads-v1.0.0-TIMESTAMP.aar          # File AAR chính
├── README.md                              # Hướng dẫn integration chi tiết
├── proguard-rules.pro                     # ProGuard rules cần thiết
└── INTEGRATION_CHECKLIST.md              # Checklist tích hợp

baseads-library.zip                        # Package đầy đủ để distribute
```

## 📋 Scripts

### 1. `build-aar.sh`
**Chức năng:** Build AAR và tạo tài liệu
**Sử dụng:**
```bash
./build-aar.sh
```
**Output:**
- File AAR với timestamp
- README với hướng dẫn integration
- ProGuard rules
- Integration checklist
- Package ZIP để distribute

### 2. `quick-release.sh`  
**Chức năng:** Release version mới với các tùy chọn
**Sử dụng:**
```bash
./quick-release.sh
```
**Options:**
- Patch release (1.0.0 → 1.0.1)
- Minor release (1.0.0 → 1.1.0)
- Major release (1.0.0 → 2.0.0)
- Custom version
- Build current version

### 3. `update-version.sh`
**Chức năng:** Cập nhật version trong build.gradle.kts
**Sử dụng:**
```bash
./update-version.sh
```

## 🔧 Configuration

### Version Management
Version được quản lý trong `base-ads/build.gradle.kts`:
```kotlin
versionName = "1.0.0"
versionCode = 1
```

### AAR Naming Convention
```
base-ads-v{version}-{timestamp}.aar
```
Ví dụ: `base-ads-v1.0.0-20251002_142159.aar`

## 📦 Integration Guide

### Cho người dùng AAR:

1. **Extract package:**
   ```bash
   unzip baseads-library.zip
   ```

2. **Copy AAR:**
   ```
   YourProject/app/libs/base-ads-v1.0.0.aar
   ```

3. **Follow README.md** trong package để integration

### Dependency Requirements:
```kotlin
// Required dependencies
implementation("com.google.android.gms:play-services-ads:22.5.0")
implementation("com.google.firebase:firebase-analytics:21.5.0")
implementation("com.ironsource.sdk:mediationsdk:7.5.1")
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
// ... see README.md for complete list
```

## 🛠️ Advanced Usage

### Custom Build Configuration
Modify `build-aar.sh` để:
- Thay đổi naming convention
- Add custom ProGuard rules
- Customize documentation templates

### Automated Releases
Integrate với CI/CD:
```yaml
# GitHub Actions example
- name: Build AAR
  run: ./build-aar.sh
  
- name: Upload artifacts
  uses: actions/upload-artifact@v3
  with:
    name: baseads-library
    path: baseads-library.zip
```

## 📚 Library Features

BaseAds library bao gồm:

- ✅ **Google Mobile Ads SDK** integration
- ✅ **ironSource mediation** support  
- ✅ **Firebase Analytics** integration
- ✅ **Adaptive banner ads** với preloading
- ✅ **Interstitial ads** với smart timing
- ✅ **VIP user management**
- ✅ **Force update functionality**
- ✅ **Jetpack Compose** UI components
- ✅ **Hilt dependency injection** ready

## 🔍 Debugging

### Build Issues
```bash
# Clean và rebuild
./gradlew :base-ads:clean
./gradlew :base-ads:assembleRelease
```

### Integration Issues
Check logs với tag:
- `BaseAds-*`
- `AdMob`
- `IronSource`

### Version Issues
```bash
# Check current version
grep -E 'version(Name|Code)' base-ads/build.gradle.kts
```

## 📈 Best Practices

### Versioning
- **Patch:** Bug fixes, minor improvements
- **Minor:** New features, API additions  
- **Major:** Breaking changes, major refactoring

### Testing
- Test AAR trong clean project trước khi distribute
- Verify tất cả dependencies work correctly
- Test trên different Android versions

### Distribution
- Include full documentation package
- Provide integration examples
- Keep change log updated

## 🆘 Support

### Common Issues
1. **AAR not building:** Check Gradle sync
2. **Missing dependencies:** Follow README.md exactly
3. **Hilt errors:** Ensure proper setup in target project
4. **Ads not showing:** Check ad unit IDs và internet

### Get Help
- Check logs với BaseAds tags
- Review integration checklist
- Test với provided example code

---

**Created:** October 2, 2025  
**BaseAds Build System v1.0.0**