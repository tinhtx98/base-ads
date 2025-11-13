# Hướng dẫn Build & Debug AAR Library

## 🎯 Tóm tắt

Đã setup xong script build AAR kèm **sources JAR** để debug trong project sử dụng.

## 📦 2 Loại Build

### 1. **Production Build** (build-aar.sh)
```bash
./build-aar.sh
```
- ✅ Code optimized
- ✅ ProGuard enabled
- ✅ File size nhỏ
- ❌ Không debug được

**Output:**
- `exported-aar/base-ads-v*.aar` - Library chính

### 2. **Debug Build** (build-debug-aar.sh) 👈 MỚI
```bash
./build-debug-aar.sh
```
- ✅ Full logging enabled
- ✅ Debug symbols
- ✅ **Sources JAR** - Debug được code
- ✅ No obfuscation
- ❌ File size lớn hơn

**Output:**
- `exported-aar-debug/base-ads-debug-v*.aar` - Library debug
- `exported-aar-debug/base-ads-debug-v*-sources.jar` - **Source code để debug**
- `baseads-debug-library.zip` - Package hoàn chỉnh

## 🚀 Cách sử dụng trong Project

### Bước 1: Copy files vào project

```bash
YourProject/
├── app/
│   ├── libs/
│   │   ├── base-ads-debug-v1.0.0-20251104_193218.aar
│   │   └── base-ads-debug-v1.0.0-20251104_193218-sources.jar  # ⭐ Quan trọng!
│   └── build.gradle.kts
```

### Bước 2: Update `app/build.gradle.kts`

```kotlin
dependencies {
    // BaseAds DEBUG Library
    implementation(files("libs/base-ads-debug-v1.0.0-20251104_193218.aar"))
    
    // REQUIRED: Manual mediation adapters (vì dùng files())
    implementation("com.google.android.gms:play-services-ads:24.6.0")
    implementation("com.google.ads.mediation:vungle:7.4.0.0")           // LiftOff
    implementation("com.google.ads.mediation:ironsource:8.4.0.0")       // IronSource  
    implementation("com.google.ads.mediation:facebook:6.17.0.0")        // Meta
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-config-ktx")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
}
```

### Bước 3: Sync Gradle

Android Studio sẽ tự động nhận diện `*-sources.jar` trong `libs/`

### Bước 4: Test Debug

1. **Ctrl/Cmd + Click** vào bất kỳ class nào của BaseAds
2. Android Studio sẽ mở **source code thực** (không phải decompiled)
3. Bạn có thể:
   - ✅ Đọc code
   - ✅ Set breakpoints
   - ✅ Step through code
   - ✅ Inspect variables
   - ✅ Xem stack traces với line numbers

## 🐛 Enable Logging

Trong `Application` class:

```kotlin
@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable full debug logging
        if (BuildConfig.DEBUG) {
            // BaseAds logging sẽ tự động bật trong debug build
        }
    }
}
```

Filter Logcat:
```
tag:BaseAds
```

## 🔄 Workflow khuyến nghị

### Development/Testing:
```bash
./build-debug-aar.sh  # Build với sources
# → Copy AAR + sources JAR vào project test
# → Debug, fix bugs
```

### Production:
```bash
./build-aar.sh  # Build optimized
# → Copy AAR vào project production
# → Deploy
```

## 📝 Technical Details

### Sources JAR được tạo như thế nào?

Trong `base-ads/build.gradle.kts`:

```kotlin
afterEvaluate {
    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(android.sourceSets.getByName("main").java.srcDirs)
    }

    tasks.named("assembleDebug") {
        finalizedBy(sourcesJar)
    }
}
```

### Android Studio nhận diện sources như thế nào?

Khi có 2 files cùng name trong `libs/`:
```
base-ads-debug-v1.0.0-*.aar
base-ads-debug-v1.0.0-*-sources.jar  # Suffix "-sources"
```

Android Studio tự động attach sources JAR vào AAR!

## ⚠️ Lưu ý quan trọng

1. **Không dùng debug build cho production** - File size lớn, performance thấp
2. **Sources JAR chỉ cần trong dev** - Không cần commit vào git
3. **Khi dùng `files()`** - Phải khai báo manual ALL mediation dependencies
4. **ProGuard rules** - Nhớ copy từ `exported-aar-debug/proguard-rules.pro`

## 🎯 So sánh 2 build types

| Feature | Production | Debug |
|---------|-----------|-------|
| File size | ~170KB | ~184KB + 36KB sources |
| Logging | ❌ Minimal | ✅ Full |
| Debug symbols | ❌ Stripped | ✅ Included |
| ProGuard | ✅ Enabled | ❌ Disabled |
| Sources | ❌ No | ✅ Yes (JAR) |
| Breakpoints | ❌ Không set được | ✅ Set được |
| Performance | ✅ Optimized | ⚠️ Slower |
| Use case | Production release | Development/Testing |

## 🚨 Troubleshooting

### "Cannot find symbol" khi dùng AAR
→ Thiếu mediation dependencies. Xem lại Bước 2.

### Không thấy sources khi Ctrl+Click
→ Kiểm tra `*-sources.jar` có cùng thư mục `libs/` với AAR không.

### eCPM = "-" cho Meta/IronSource/LiftOff
→ Thiếu mediation adapters. Phải khai báo manual vì dùng `files()`.

### Build production bị lỗi ProGuard
→ Copy rules từ `exported-aar/proguard-rules.pro` vào project.

## 📚 Files quan trọng

```
BaseAds/
├── build-aar.sh                    # Production build
├── build-debug-aar.sh              # Debug build (mới)
├── base-ads/
│   ├── build.gradle.kts           # Config sources JAR generation
│   ├── proguard-rules.pro         # ProGuard rules (Meta fix)
│   └── consumer-rules.pro         # Rules export to consuming apps
├── exported-aar/                  # Production output
└── exported-aar-debug/            # Debug output
    ├── *.aar                      # Debug library
    ├── *-sources.jar              # ⭐ Sources for debugging
    ├── DEBUG-README.md            # Documentation
    └── prompts/                   # AI prompts for integration
```

## ✨ Next Steps

1. ✅ Build debug AAR: `./build-debug-aar.sh`
2. ✅ Copy AAR + sources JAR vào project test
3. ✅ Sync Gradle
4. ✅ Test debug bằng Ctrl+Click vào BaseAds code
5. ✅ Set breakpoints và debug
6. ✅ Fix bugs nếu cần
7. ✅ Build production: `./build-aar.sh` khi ready

Happy debugging! 🐛✨
