# 🚀 Force Update với Firebase Remote Config

## 📋 Tổng quan

Module Force Update sử dụng Firebase Remote Config để bắt buộc người dùng cập nhật ứng dụng khi có phiên bản mới. Hỗ trợ:

- ✅ **Real-time updates** - Cập nhật config mà không cần restart app
- ✅ **Flexible conditions** - Force bằng flag hoặc version code mismatch
- ✅ **Custom UI** - Tùy chỉnh title, message từ Remote Config
- ✅ **Smart routing** - Mở Play Store app, fallback sang browser
- ✅ **Analytics tracking** - Log sự kiện force_update_shown và force_update_click

---

## 🏗️ Kiến trúc

```
forceupdate/
├── data/
│   ├── RemoteConfigDataSource.kt    # Firebase Remote Config operations
│   └── ForceUpdateRepository.kt      # State management & Flow
├── domain/
│   ├── ForceUpdateConfig.kt          # Data model
│   └── EvaluateForceUpdateUseCase.kt # Business logic
├── presentation/
│   ├── ForceUpdateViewModel.kt       # ViewModel with Hilt
│   ├── ForceUpdateState.kt           # UI state model
│   └── ForceUpdateDialog.kt          # Compose UI
└── di/
    └── ForceUpdateModule.kt          # Dependency injection
```

---

## ⚙️ Cấu hình Firebase Remote Config

### 1. Tạo tham số trong Firebase Console

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Chọn project → **Remote Config**
3. Thêm tham số mới:
   - **Key:** `force_update_json`
   - **Type:** String
   - **Value:** JSON theo format bên dưới

### 2. Format JSON

```json
{
  "latest_version_code": 120,
  "latest_version_name": "2.3.0",
  "force_now": false,
  "title": "Cập nhật bắt buộc",
  "message": "Phiên bản mới đã sẵn sàng. Vui lòng cập nhật để tiếp tục sử dụng.",
  "store_url": "https://play.google.com/store/apps/details?id=com.tinhtx.baseads"
}
```

### 3. Giải thích các field

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `latest_version_code` | Int | No | Version code mới nhất trên Play Store |
| `latest_version_name` | String | No | Version name để hiển thị (VD: "2.3.0") |
| `force_now` | Boolean | Yes | Force update ngay lập tức (bỏ qua check version) |
| `title` | String | No | Title dialog (null = dùng default) |
| `message` | String | No | Message dialog (null = dùng default) |
| `store_url` | String | No | Custom Play Store URL (null = auto-generate) |

---

## 🎯 Logic Force Update

App sẽ hiển thị dialog force update khi **MỘT TRONG HAI** điều kiện đúng:

### Điều kiện 1: Flag force_now = true
```kotlin
if (config.forceNow == true) {
    // Show dialog ngay lập tức
    // Không cần check version code
}
```

### Điều kiện 2: Version code khác nhau
```kotlin
if (config.latestVersionCode != BuildConfig.VERSION_CODE) {
    // Show dialog vì có version mới
}
```

### Flow hoạt động

```
App Launch
    ↓
Application.onCreate()
    ↓
ForceUpdateRepository.init()
    ↓
Fetch & Activate Remote Config
    ↓
Parse JSON → ForceUpdateConfig
    ↓
EvaluateForceUpdateUseCase
    ↓
Check conditions
    ├─ force_now = true? → Show Dialog
    ├─ latest_version_code != current? → Show Dialog
    └─ else → No dialog
```

---

## 🔄 Real-time Updates

Firebase Remote Config Realtime cho phép cập nhật config mà không cần restart app:

```kotlin
// Tự động setup trong RemoteConfigDataSource
remoteConfig.addOnConfigUpdateListener { configUpdate ->
    remoteConfig.activate().addOnComplete {
        // Config updated → re-evaluate force update
    }
}
```

**Khi config thay đổi trên Firebase Console:**
1. ✅ Listener nhận update trong vài giây
2. ✅ Activate config mới
3. ✅ Re-evaluate điều kiện force update
4. ✅ Show/hide dialog tự động

---

## 🧪 Testing

### Test 1: Force bằng flag
```json
{
  "force_now": true
}
```
**Kết quả:** Dialog hiện ngay lập tức, không cần check version

### Test 2: Force bằng version mismatch
```json
{
  "latest_version_code": 999,
  "force_now": false
}
```
**Kết quả:** Dialog hiện vì 999 != BuildConfig.VERSION_CODE (1)

### Test 3: Custom UI
```json
{
  "force_now": true,
  "title": "Update Required!",
  "message": "Please update to continue using the app.",
  "store_url": "https://play.google.com/store/apps/details?id=com.tinhtx.baseads"
}
```
**Kết quả:** Dialog với custom title/message

### Test 4: No force update
```json
{
  "latest_version_code": 1,
  "force_now": false
}
```
**Kết quả:** Không hiện dialog (version code match)

---

## 📱 User Experience

### 1. Dialog hiển thị
- ❌ **Không thể dismiss** bằng cách tap ra ngoài
- ❌ **Không có nút "Hủy"** hoặc "Đóng"
- ✅ **Chỉ có nút "Cập nhật"**

### 2. Khi nhấn "Cập nhật"
1. **Try 1:** Mở Play Store app với `market://` URI
2. **Try 2:** Nếu không có Play Store, mở browser với https://play.google.com
3. **Analytics:** Log event `force_update_click`

### 3. Session management
- User click "Cập nhật" → Flag set để không spam dialog
- Config thay đổi → Reset flag, show lại nếu vẫn cần force

---

## 🔧 Customization

### Thay đổi logic so sánh version

Mặc định dùng `!=` (khác nhau). Nếu muốn dùng `<` (nhỏ hơn):

```kotlin
// File: EvaluateForceUpdateUseCase.kt
operator fun invoke(config: ForceUpdateConfig, appVersionCode: Int): Boolean {
    if (config.forceNow) return true
    
    val latestVersion = config.latestVersionCode
    // Thay != thành <
    return latestVersion != null && appVersionCode < latestVersion
}
```

### Thêm nút "Đóng app"

```kotlin
// File: ForceUpdateDialog.kt
AlertDialog(
    // ...
    dismissButton = {
        TextButton(onClick = { exitProcess(0) }) {
            Text("Đóng app")
        }
    }
)
```

### Thay đổi fetch interval

```kotlin
// File: ForceUpdateModule.kt
val configSettings = FirebaseRemoteConfigSettings.Builder()
    .setMinimumFetchIntervalInSeconds(
        if (BuildConfig.DEBUG) 0L else 7200L // 2 hours
    )
    .build()
```

---

## 📊 Analytics Events

### Event: force_update_shown
```kotlin
{
    "latest_version_code": 120,
    "current_version_code": 1,
    "force_now": false
}
```

### Event: force_update_click
```kotlin
{
    "version_code": 1
}
```

---

## 🐛 Troubleshooting

### Dialog không hiện

**Check 1:** Verify Remote Config trong Firebase Console
```bash
# Check trong Logcat
RemoteConfigDS: force_update_json is empty
```
→ Tạo tham số `force_update_json` trên Firebase Console

**Check 2:** Verify JSON format
```bash
RemoteConfigDS: Failed to parse force_update_json
```
→ Check JSON syntax (dùng JSONLint)

**Check 3:** Check điều kiện
```bash
ForceUpdateVM: Should force update: false
```
→ Set `force_now = true` để test

### Play Store không mở

**Check Logcat:**
```bash
ForceUpdateDialog: Play Store not available, opening browser
```
→ Normal trên emulator không có Play Store

### Realtime không hoạt động

**Check Firebase Console:**
- Realtime updates phải được enable
- App phải connected to internet
- Đợi vài giây để listener trigger

---

## ✅ Acceptance Criteria

- [x] `force_now=true` → Dialog hiện ngay
- [x] `latest_version_code != current` → Dialog hiện
- [x] Cả hai điều kiện false → Không hiện dialog
- [x] Nút "Cập nhật" mở Play Store hoặc browser
- [x] Dialog không bị dismiss khi force update
- [x] Realtime updates hoạt động không cần restart
- [x] Không crash khi xoay màn hình
- [x] Code có comment đầy đủ
- [x] Tách layer rõ ràng (data/domain/presentation)
- [x] Analytics tracking đầy đủ

---

## 📚 Dependencies

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
implementation("com.google.firebase:firebase-config-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")

// Hilt
implementation("com.google.dagger:hilt-android:2.56.1")
ksp("com.google.dagger:hilt-compiler:2.56.1")

// Compose
implementation(platform("androidx.compose:compose-bom:2024.12.01"))
implementation("androidx.compose.material3:material3")
```

---

## 🚀 Setup Checklist

- [ ] Add Firebase to project (`google-services.json`)
- [ ] Add dependencies to `build.gradle.kts`
- [ ] Create `force_update_json` parameter in Firebase Console
- [ ] Set initial value with `force_now=false`
- [ ] Enable Realtime Config Updates
- [ ] Test with `force_now=true`
- [ ] Test with version mismatch
- [ ] Test Play Store redirect
- [ ] Monitor analytics events

---

## 📞 Support

Nếu gặp vấn đề:
1. Check Logcat với filter: `ForceUpdate` hoặc `RemoteConfig`
2. Verify Firebase Console config
3. Test với `force_now=true` để đơn giản
4. Check internet connection

---

**Build Status:** ✅ READY FOR PRODUCTION  
**Last Updated:** October 2, 2025  
**Version:** 1.0.0
