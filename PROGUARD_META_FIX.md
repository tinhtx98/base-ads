# Fix ProGuard Issues với Meta Audience Network

## Vấn đề
Khi build production (release) với R8/ProGuard enabled, gặp lỗi:
```
Missing class com.facebook.infer.annotation.Nullsafe$Mode
Missing class com.facebook.infer.annotation.Nullsafe
```

## Nguyên nhân
- Meta (Facebook) SDK sử dụng các annotation classes từ package `com.facebook.infer.annotation`
- ProGuard/R8 không thể tìm thấy các classes này khi obfuscate code
- Cần thêm rules để keep các classes này

## Giải pháp

### 1. Cập nhật ProGuard Rules trong Library

File: `base-ads/proguard-rules.pro`

```proguard
# Keep Meta Audience Network (Facebook) SDK classes
-keep class com.facebook.ads.** { *; }
-keepclassmembers class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**
-dontwarn com.facebook.infer.annotation.**
-keep class com.facebook.infer.annotation.** { *; }
```

### 2. Copy rules vào exported AAR

File: `exported-aar/proguard-rules.pro` cũng cần có rules tương tự.

### 3. Sử dụng trong Project

Khi integrate AAR vào project khác, đảm bảo:

**build.gradle (app module):**
```kotlin
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "libs/base-ads-proguard-rules.pro" // Include ProGuard rules từ AAR
            )
        }
    }
}

dependencies {
    implementation(files("libs/base-ads-v1.0.0-20251102_204306.aar"))
    
    // Meta Audience Network
    implementation("com.facebook.android:audience-network-sdk:latest.version")
    
    // Other mediation SDKs
    implementation(libs.play.services.ads)
    implementation(libs.vungle)
    implementation(libs.mediationsdk)
}
```

**Hoặc thêm trực tiếp vào proguard-rules.pro của app:**
```proguard
# Meta Audience Network
-keep class com.facebook.ads.** { *; }
-keepclassmembers class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**
-dontwarn com.facebook.infer.annotation.**
-keep class com.facebook.infer.annotation.** { *; }
```

## Các Rules bổ sung (Recommended)

Thêm các rules sau để đảm bảo Meta SDK hoạt động tốt:

```proguard
# Meta SDK - WebView và Native interfaces
-keepclassmembers class * implements android.webkit.WebViewClient {
    <methods>;
}

# Meta SDK - Serialization
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses

# Meta SDK - Models/DTOs
-keep class com.facebook.ads.internal.** { *; }
```

## Rebuild AAR

Sau khi cập nhật ProGuard rules, rebuild AAR:

```bash
./build-aar.sh
```

## Testing

1. Build release APK với minifyEnabled = true
2. Kiểm tra không còn warnings về Meta classes
3. Test ads hoạt động bình thường trong release build

## Notes

- **Quan trọng**: Luôn copy file `proguard-rules.pro` cùng với AAR khi distribute
- Nếu vẫn gặp issues, có thể cần thêm `-dontwarn` cho các packages khác của Meta
- Check Meta SDK documentation cho ProGuard rules mới nhất: https://developers.facebook.com/docs/audience-network/android

## Related Files

- `base-ads/proguard-rules.pro` - Source ProGuard rules
- `exported-aar/proguard-rules.pro` - Distributed ProGuard rules
- `base-ads/consumer-rules.pro` - Consumer ProGuard rules (tự động apply khi import AAR)

## Troubleshooting

### Vẫn còn lỗi sau khi thêm rules?

1. **Clean & Rebuild**:
```bash
./gradlew clean
./gradlew assembleRelease
```

2. **Kiểm tra version compatibility**:
   - Meta SDK version có compatible với Google Mobile Ads SDK không?
   - Check version trong `libs.versions.toml`

3. **Enable ProGuard debugging**:
```proguard
-printconfiguration proguard-config.txt
-printusage proguard-usage.txt
```

4. **Xem chi tiết R8/ProGuard output**:
```bash
./gradlew assembleRelease --info
```
