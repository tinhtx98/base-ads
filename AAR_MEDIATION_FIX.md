# Fix: eCPM = "-" cho Meta, LiftOff, IronSource khi dùng AAR Library

## 🔴 Vấn đề

Khi build `base-ads` thành AAR library và import vào project khác:
- ✅ **AdMob** hoạt động bình thường, hiển thị eCPM
- ❌ **Meta, LiftOff, IronSource** hiển thị eCPM = "-" (không bidding được)

## 🎯 Nguyên nhân

### **Sự khác biệt giữa `implementation` vs `api` trong AAR:**

```kotlin
// ❌ SAI - Khi build AAR
dependencies {
    implementation("com.google.ads.mediation:vungle:7.4.0.0")
    implementation(libs.ironsource.mediation.adapter)
    implementation(libs.meta.mediation.adapter)
}
```

**Điều gì xảy ra:**
1. Các mediation adapters được compile vào AAR
2. Nhưng **KHÔNG được export** trong POM dependencies
3. Project chính không thể truy cập các adapter classes
4. AdMob không load được adapters → eCPM = "-"

**Tại sao AdMob vẫn work?**
- Project chính đã tự import: `com.google.android.gms:play-services-ads`
- Nên AdMob SDK có sẵn trong runtime

### **Giải pháp:**

```kotlin
// ✅ ĐÚNG - Export adapters trong AAR
dependencies {
    api(libs.play.services.ads.v2460)
    api("com.google.ads.mediation:vungle:7.4.0.0")
    api(libs.ironsource.mediation.adapter)
    api(libs.meta.mediation.adapter)
}
```

**Kết quả:**
1. Adapters được compile vào AAR
2. Adapters được **export trong POM** (transitive dependencies)
3. Project chính tự động có tất cả adapters
4. Bidding mediation hoạt động đầy đủ ✅

## 📋 Các bước áp dụng

### 1. Sửa `base-ads/build.gradle.kts`

Đổi các mediation dependencies từ `implementation` → `api`:

```kotlin
dependencies {
    // Google Mobile Ads SDK
    api(libs.play.services.ads.v2460)
    
    // Mediation adapters - Must use 'api' for AAR export
    api("com.google.ads.mediation:vungle:7.4.0.0")
    api(libs.ironsource.mediation.adapter)
    api(libs.meta.mediation.adapter)
    
    // Other dependencies can stay 'implementation'
    implementation(platform(libs.firebase.bom.v3351))
    implementation(libs.google.firebase.analytics.ktx)
    implementation(libs.hilt.android)
    // ...
}
```

### 2. Rebuild AAR

```bash
./gradlew clean :base-ads:assembleRelease
```

### 3. Project chính - Xóa duplicate dependency

Trong `app/build.gradle.kts`, **XÓA** dòng này:

```kotlin
// ❌ XÓA - Đã có trong base-ads với 'api'
// implementation(libs.google.mobile.ads)
```

**Lý do:** AdMob SDK đã được export từ `base-ads` qua `api`, không cần import lại.

### 4. Test mediation

Chạy app và kiểm tra:
- AdMob Mediation Test Suite
- Check logcat cho tất cả adapters được load:
  ```
  I/Ads: Adapter initialized: com.google.ads.mediation.vungle
  I/Ads: Adapter initialized: com.ironsource.adapters.admob
  I/Ads: Adapter initialized: com.google.ads.mediation.facebook
  ```

## 🔍 Kiểm tra AAR có export dependencies

Giải nén AAR và check POM file:

```bash
cd base-ads/build/outputs/aar
unzip -q base-ads-release.aar -d temp
cat temp/pom.xml
```

Phải thấy các dependencies này trong `<dependencies>`:
```xml
<dependency>
    <groupId>com.google.android.gms</groupId>
    <artifactId>play-services-ads</artifactId>
    <version>24.6.0</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.google.ads.mediation</groupId>
    <artifactId>vungle</artifactId>
    <version>7.4.0.0</version>
    <scope>compile</scope>
</dependency>
<!-- ... Meta, IronSource ... -->
```

## 📚 Rule of thumb cho AAR library

### Dùng `api` khi:
- ✅ Library cần expose SDK cho consuming apps
- ✅ Mediation adapters
- ✅ Core dependencies mà consuming app cần interact

### Dùng `implementation` khi:
- ✅ Internal utilities
- ✅ Compose, Navigation, AndroidX (không cần expose)
- ✅ Firebase Analytics (internal logging)
- ✅ Hilt, Coroutines (compile-time only)

## 🎉 Kết quả

Sau khi apply fix:
- ✅ AdMob: có eCPM
- ✅ Meta: có eCPM 
- ✅ LiftOff: có eCPM
- ✅ IronSource: có eCPM

Tất cả bidding partners hoạt động bình thường! 🚀

## 🔗 Tham khảo

- [Gradle Dependencies: api vs implementation](https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_separation)
- [AdMob Mediation Guide](https://developers.google.com/admob/android/mediation)
- [Creating Android Library AAR](https://developer.android.com/studio/projects/android-library)
