# 🐛 Quick Debug Guide - Banner Size Issues

## 🚀 Fast Fix Summary

### **✅ Đã Fix:**
1. Banner size calculation - CORRECT
2. Height calculation - CORRECT  
3. Width handling - CORRECT (fillMaxWidth)

### **🔧 Để Debug Layout Issues:**

#### **Step 1: Use Debug Banner**

Trong `MainActivity.kt`, thay `AdaptiveBanner` bằng `AdaptiveBannerDebug`:

```kotlin
// OLD
AdaptiveBanner(
    adUnitsProvider = adUnitsProvider,
    adsConfig = adsConfig,
    vipGate = vipGate,
    analyticsLogger = analyticsLogger
)

// NEW - Debug version
AdaptiveBannerDebug(
    adUnitsProvider = adUnitsProvider,
    adsConfig = adsConfig,
    vipGate = vipGate,
    analyticsLogger = analyticsLogger,
    showDebugInfo = true // Shows size info
)
```

#### **Step 2: Understand Debug Colors**

- 🔴 **Red Background** = Banner Container (available space)
- 🔵 **Blue Background** = Actual Banner Ad
- **If Red shows**: Banner is smaller than container → padding issue
- **If no Red**: Banner fills container perfectly ✅

#### **Step 3: Check Logcat**

```bash
adb logcat | grep "BaseAds-Banner"
```

Look for:
```
D/BaseAds-Banner: Screen width: 392dp, pixels: 1176
D/BaseAds-Banner: AdSize requested: 392dp, AdSize returned: 392x50dp  
D/BaseAds-Banner: Banner height: 50.0dp (150px)
```

## 🎯 Common Issues & Solutions

### **Issue 1: Banner Nhỏ Hơn Container**

**Nguyên nhân:** Padding hoặc constraint từ parent

**Fix:**
```kotlin
// ❌ WRONG
Column(modifier = Modifier.padding(16.dp)) {
    AdaptiveBanner(...) // Will be 32dp narrower!
}

// ✅ CORRECT
Column(modifier = Modifier.fillMaxWidth()) {
    Box(modifier = Modifier.padding(16.dp)) {
        // Content với padding
    }
    AdaptiveBanner(...) // No padding = full width
}
```

### **Issue 2: Banner Bị Cut Off**

**Nguyên nhân:** Parent có height constraint

**Fix:**
```kotlin
// ❌ WRONG  
Column(modifier = Modifier.height(300.dp)) {
    AdaptiveBanner(...) // Might be cut off
}

// ✅ CORRECT
Column(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.weight(1f)) { 
        // Content 
    }
    AdaptiveBanner(...) // At bottom, no height constraint
}
```

### **Issue 3: White Space Around Banner**

**Nguyên nhân:** Banner modifier có padding

**Fix:**
```kotlin
// ❌ WRONG
AdaptiveBanner(
    modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp) // This creates white space!
)

// ✅ CORRECT
AdaptiveBanner(
    modifier = Modifier.fillMaxWidth()
    // No padding!
)
```

## 📱 Test Checklist

1. ✅ **Build & Install:**
   ```bash
   ./gradlew installDebug
   ```

2. ✅ **Run app** and navigate to screen với banner

3. ✅ **Check visually:**
   - Banner fills full width? 
   - No white space around it?
   - Looks professional?

4. ✅ **Check logcat:**
   ```bash
   adb logcat | grep "BaseAds-Banner"
   ```

5. ✅ **If still có issue**, use `AdaptiveBannerDebug` to see:
   - Red background = where is extra space
   - Blue background = actual banner size

## 🎊 Expected Result

### **✅ Success Indicators:**

1. Banner **fills full screen width**
2. **No red background** visible (when using debug version)
3. Logcat shows: `AdSize returned: [ScreenWidth]x50dp`
4. Banner height = **50dp** (phone) hoặc **90dp** (tablet)
5. **No white space** or gaps

### **❌ If Still Has Issues:**

Check these trong code của bạn:

```kotlin
// 1. Parent Column
Column(modifier = Modifier.fillMaxWidth()) { // ✅ Must be fillMaxWidth
    
    // 2. Wrapper Box (if any)
    // ❌ Don't wrap banner in constrained Box
    
    // 3. Banner itself
    AdaptiveBanner(
        modifier = Modifier.fillMaxWidth() // ✅ Must have this
    )
}
```

## 🔄 Switch Back to Production

Sau khi debug xong, switch về production banner:

```kotlin
// Debug version (for troubleshooting)
AdaptiveBannerDebug(
    showDebugInfo = true
)

// Production version (normal use)
AdaptiveBanner(
    adUnitsProvider = adUnitsProvider,
    adsConfig = adsConfig,
    vipGate = vipGate,
    analyticsLogger = analyticsLogger
)
```

---

**🎯 Fix đã được apply! Rebuild và test ngay!**

Test command:
```bash
./gradlew installDebug && adb logcat | grep "BaseAds-Banner"
```