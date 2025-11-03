# Checklist: Fix ProGuard Meta SDK Issue

## ✅ Đã hoàn thành

- [x] Cập nhật `base-ads/proguard-rules.pro` với Meta SDK rules
- [x] Cập nhật `base-ads/consumer-rules.pro` với Meta SDK rules (tự động apply khi import AAR)
- [x] Cập nhật `exported-aar/proguard-rules.pro` với Meta SDK rules
- [x] Tạo file hướng dẫn `PROGUARD_META_FIX.md`

## 🔄 Cần thực hiện tiếp

### Bước 1: Rebuild AAR
```bash
cd c:\Users\tinhtx\Documents\GitHub\base-ads
./build-aar.sh
```

Hoặc build debug version để test nhanh:
```bash
./build-debug-aar.sh
```

### Bước 2: Copy AAR mới vào project

1. AAR mới sẽ được tạo trong `exported-aar/` với tên có timestamp
2. Copy file AAR mới vào `libs/` folder của project đích
3. Update tên file trong `build.gradle`:
```kotlin
implementation(files("libs/base-ads-v1.0.0-[NEW_TIMESTAMP].aar"))
```

### Bước 3: Thêm ProGuard rules vào project đích

**Option 1 (Recommended)**: Copy file ProGuard rules
```bash
# Copy file proguard từ exported-aar
cp exported-aar/proguard-rules.pro [PROJECT_PATH]/app/base-ads-proguard-rules.pro
```

Sau đó update `build.gradle` (app):
```kotlin
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "base-ads-proguard-rules.pro"  // Add this line
            )
        }
    }
}
```

**Option 2**: Thêm trực tiếp vào `app/proguard-rules.pro`
```proguard
# Meta Audience Network
-keep class com.facebook.ads.** { *; }
-keepclassmembers class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**
-dontwarn com.facebook.infer.annotation.**
-keep class com.facebook.infer.annotation.** { *; }
```

### Bước 4: Clean & Build Release

```bash
cd [PROJECT_PATH]
./gradlew clean
./gradlew assembleRelease
```

### Bước 5: Verify

Kiểm tra build output không còn warnings về:
- ✅ `Missing class com.facebook.infer.annotation.Nullsafe`
- ✅ `Missing class com.facebook.infer.annotation.Nullsafe$Mode`

### Bước 6: Test Ads

1. Install release APK trên device
2. Test các ad formats:
   - Banner ads
   - Interstitial ads
   - Rewarded ads
3. Kiểm tra Meta bidding hoạt động trong mediation waterfall

## 📝 Notes quan trọng

### Consumer Rules tự động apply
File `consumer-rules.pro` đã được cập nhật, nên ProGuard rules sẽ **TỰ ĐỘNG** được apply khi import AAR. Tuy nhiên, để chắc chắn, nên:
1. Copy thêm file `proguard-rules.pro` từ `exported-aar/`
2. Hoặc thêm rules trực tiếp vào app

### Kiểm tra versions
Đảm bảo Meta SDK version compatible:
```kotlin
// Check trong build.gradle
implementation("com.facebook.android:audience-network-sdk:X.X.X")
```

Recommended: Sử dụng version mới nhất từ Maven Central

### Debug ProGuard issues

Nếu vẫn gặp vấn đề, enable debug output:
```kotlin
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles(...)
            
            // Add for debugging
            proguardFiles.each { file ->
                if (file.exists()) {
                    logger.lifecycle("ProGuard file: ${file.absolutePath}")
                }
            }
        }
    }
}
```

Xem log chi tiết:
```bash
./gradlew assembleRelease --info | grep -i "proguard\|r8"
```

## 🎯 Expected Result

Sau khi hoàn thành các bước trên:
- ✅ Build release thành công không có warnings về Meta classes
- ✅ Ads hiển thị bình thường trong release build
- ✅ Meta bidding hoạt động trong mediation waterfall
- ✅ Không crash khi show ads

## 🆘 Nếu vẫn gặp issues

1. Check file `PROGUARD_META_FIX.md` để xem troubleshooting steps
2. Verify Meta SDK được add đúng trong dependencies
3. Check AdMob dashboard xem Meta adapter có được enable không
4. Test với debug build trước để isolate ProGuard issues
