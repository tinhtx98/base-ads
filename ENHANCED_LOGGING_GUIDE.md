# 📊 Enhanced Ads Logging - Complete Guide

## 🎯 Overview

Module Base Ads giờ có **enhanced logging system** với khả năng:
- ✅ Track từng ad instance riêng biệt
- ✅ Hiển thị status rõ ràng (Ready/Not Ready/Failed/etc.)
- ✅ Phân biệt ads nào được init thành công
- ✅ Visual status summary với emojis
- ✅ Real-time status updates

---

## 📋 Ad Status Types

```kotlin
enum class AdStatus {
    INITIALIZING,    // ⏳ Ad đang được load
    READY,          // ✅ Ad loaded và ready to show
    SHOWING,        // 📺 Ad đang được hiển thị
    FAILED,         // ❌ Ad failed to load
    DISMISSED,      // ✓ Ad đã show và dismissed
    NOT_AVAILABLE   // 🚫 Ad không available (VIP, disabled, etc.)
}
```

---

## 🔍 Log Output Examples

### 1. **Ad Registration (Init Start)**

```
D/BaseAds-AdRegistry: [banner] Ad 'banner_bottom' registered - Status: INITIALIZING
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 📊 ADS STATUS SUMMARY (1 total)
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 🎯 BANNER ADS (1):
D/BaseAds-AdRegistry:   ⏳ banner_bottom: INITIALIZING (age: 0s)
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 📈 STATUS BREAKDOWN:
D/BaseAds-AdRegistry:   ✅ Ready: 0
D/BaseAds-AdRegistry:   ⏳ Initializing: 1
D/BaseAds-AdRegistry:   ❌ Failed: 0
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### 2. **Ad Successfully Loaded**

```
I/BaseAds-AdRegistry: [banner] Ad 'banner_bottom' ✅ Status: READY
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 📊 ADS STATUS SUMMARY (1 total)
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 🎯 BANNER ADS (1):
D/BaseAds-AdRegistry:   ✅ banner_bottom: READY (age: 2s)
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 📈 STATUS BREAKDOWN:
D/BaseAds-AdRegistry:   ✅ Ready: 1
D/BaseAds-AdRegistry:   ⏳ Initializing: 0
D/BaseAds-AdRegistry:   ❌ Failed: 0
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### 3. **Ad Failed to Load**

```
E/BaseAds-AdRegistry: [banner] Ad 'banner_1' ❌ Status: FAILED - Error: Code: 3 - No fill.
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 📊 ADS STATUS SUMMARY (2 total)
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 🎯 BANNER ADS (2):
D/BaseAds-AdRegistry:   ✅ banner_bottom: READY (age: 5s)
D/BaseAds-AdRegistry:   ❌ banner_1: FAILED - Code: 3 - No fill. (age: 1s)
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 📈 STATUS BREAKDOWN:
D/BaseAds-AdRegistry:   ✅ Ready: 1
D/BaseAds-AdRegistry:   ⏳ Initializing: 0
D/BaseAds-AdRegistry:   ❌ Failed: 1
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### 4. **Multiple Ads (Mixed Status)**

```
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 📊 ADS STATUS SUMMARY (5 total)
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 🎯 BANNER ADS (4):
D/BaseAds-AdRegistry:   ✅ banner_bottom: READY (age: 10s)
D/BaseAds-AdRegistry:   ✅ banner_1: READY (age: 8s)
D/BaseAds-AdRegistry:   ⏳ banner_2: INITIALIZING (age: 1s)
D/BaseAds-AdRegistry:   ❌ banner_3: FAILED - Code: 3 - No fill. (age: 3s)
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 📽️ INTERSTITIAL ADS (1):
D/BaseAds-AdRegistry:   ✅ interstitial_main: READY (age: 15s)
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 📈 STATUS BREAKDOWN:
D/BaseAds-AdRegistry:   ✅ Ready: 3
D/BaseAds-AdRegistry:   ⏳ Initializing: 1
D/BaseAds-AdRegistry:   ❌ Failed: 1
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### 5. **VIP User (Ads Not Available)**

```
D/BaseAds-AdRegistry: [banner] Ad 'banner_bottom' registered - Status: INITIALIZING
D/BaseAds-AdRegistry: [banner] Ad 'banner_bottom' 🚫 Status: NOT_AVAILABLE - Error: VIP user
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 📊 ADS STATUS SUMMARY (1 total)
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 🎯 BANNER ADS (1):
D/BaseAds-AdRegistry:   🚫 banner_bottom: NOT_AVAILABLE - VIP user (age: 0s)
```

---

## 🔧 How to View Logs

### **Option 1: Filter by Tag**

```bash
# All ads logs
adb logcat | grep "BaseAds"

# Only ad registry/status logs
adb logcat | grep "BaseAds-AdRegistry"

# Specific ad type
adb logcat | grep "BaseAds-Banner"
adb logcat | grep "BaseAds-Interstitial"
```

### **Option 2: Filter by Status**

```bash
# Only successful ads (Ready)
adb logcat | grep "✅"

# Only failed ads
adb logcat | grep "❌"

# Only initializing ads
adb logcat | grep "⏳"
```

### **Option 3: Complete Ad Lifecycle**

```bash
# See everything
adb logcat -s BaseAds-AdRegistry BaseAds-Banner BaseAds-Interstitial BaseAds-BannerAdItem BaseAds-BannerAdCard
```

---

## 📊 Ad Instance Tracking

### **Tracked Ads:**

| Ad ID | Type | Location |
|-------|------|----------|
| `banner_bottom` | Banner | Bottom bar (Scaffold bottomBar) |
| `banner_1`, `banner_2`, etc. | Banner | List items (LazyColumn) |
| `interstitial_main` | Interstitial | Navigation/actions |

### **Ad Lifecycle:**

```
1. REGISTERED → Ad được tạo
   ⏳ INITIALIZING → Bắt đầu load
   
2a. ✅ READY → Load thành công, ready to show
    📺 SHOWING → Đang hiển thị cho user
    ✓ DISMISSED → User đã dismiss (interstitial only)
    
2b. ❌ FAILED → Load thất bại (with error message)

2c. 🚫 NOT_AVAILABLE → VIP user hoặc ads disabled
```

---

## 🎯 Status Summary Breakdown

### **Emojis Meaning:**

| Emoji | Status | Meaning |
|-------|--------|---------|
| ⏳ | INITIALIZING | Đang load, chờ response từ ad network |
| ✅ | READY | Ad sẵn sàng để show |
| 📺 | SHOWING | Ad đang hiển thị |
| ✓ | DISMISSED | Ad đã dismiss (interstitial) |
| ❌ | FAILED | Ad failed, xem error message |
| 🚫 | NOT_AVAILABLE | VIP user hoặc disabled |

### **Status Breakdown Counts:**

```
📈 STATUS BREAKDOWN:
  ✅ Ready: 3        ← Số ads đang ready to show
  ⏳ Initializing: 1 ← Số ads đang load
  ❌ Failed: 1       ← Số ads bị fail
```

---

## 🛠️ Debugging with Enhanced Logs

### **Problem: Ads không hiển thị**

**Check logs for:**

```bash
adb logcat | grep "BaseAds-AdRegistry"
```

**Look for:**
1. ✅ **READY status** → Ad loaded successfully
   - If not READY, check why (FAILED? NOT_AVAILABLE?)
   
2. ❌ **FAILED status** → Read error message
   ```
   ❌ banner_1: FAILED - Code: 3 - No fill.
   ```
   - Code 3 = No ad inventory (normal in test)
   - Code 0 = Network error
   - Code 1 = Invalid request
   
3. 🚫 **NOT_AVAILABLE** → Check reason
   ```
   🚫 banner_bottom: NOT_AVAILABLE - VIP user
   ```
   - VIP user → Expected behavior
   - Banner disabled → Check AdsConfig

### **Problem: Ads nháy/flicker**

**Check logs:**
```bash
adb logcat | grep "BaseAds-BannerAdItem"
```

**Look for:**
- Multiple "Creating AdView" for same ID → Missing stable keys
- Frequent "Disposed" → Recomposition issues

**See:** `FIX_BANNER_FLICKERING.md` for solution

### **Problem: Interstitial không show**

**Check logs:**
```bash
adb logcat | grep "BaseAds-Interstitial\|BaseAds-AdRegistry"
```

**Look for:**
1. Status of `interstitial_main`:
   ```
   ✅ interstitial_main: READY → Ad is loaded
   ```
   
2. Policy check logs:
   ```
   D/BaseAds-Policy: All policy checks passed
   ```
   
3. If not READY:
   ```
   ❌ interstitial_main: FAILED - Code: 3 - No fill.
   ```

---

## 📝 Log Format Reference

### **Ad Registration:**
```
D/BaseAds-AdRegistry: [<type>] Ad '<id>' registered - Status: INITIALIZING
```

### **Status Update:**
```
I/BaseAds-AdRegistry: [<type>] Ad '<id>' <emoji> Status: <status>
E/BaseAds-AdRegistry: [<type>] Ad '<id>' <emoji> Status: <status> - Error: <error>
```

### **Status Summary:**
```
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 📊 ADS STATUS SUMMARY (<count> total)
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 🎯 BANNER ADS (<count>):
D/BaseAds-AdRegistry:   <emoji> <id>: <status> (age: <seconds>s)
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 📽️ INTERSTITIAL ADS (<count>):
D/BaseAds-AdRegistry:   <emoji> <id>: <status> (age: <seconds>s)
D/BaseAds-AdRegistry: 
D/BaseAds-AdRegistry: 📈 STATUS BREAKDOWN:
D/BaseAds-AdRegistry:   ✅ Ready: <count>
D/BaseAds-AdRegistry:   ⏳ Initializing: <count>
D/BaseAds-AdRegistry:   ❌ Failed: <count>
D/BaseAds-AdRegistry: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## 🎊 Benefits

### **Before (Old Logs):**
```
D/BaseAds-Banner: Creating AdView
D/BaseAds-Banner: Banner ad loaded successfully
D/BaseAds-Banner: Creating AdView    ← Which banner?
D/BaseAds-Banner: Banner ad failed   ← Which one failed?
```

### **After (Enhanced Logs):**
```
D/BaseAds-AdRegistry: [banner] Ad 'banner_bottom' ✅ Status: READY
D/BaseAds-AdRegistry: [banner] Ad 'banner_1' ⏳ Status: INITIALIZING
D/BaseAds-AdRegistry: [banner] Ad 'banner_2' ❌ Status: FAILED - Code: 3
D/BaseAds-AdRegistry: 📊 STATUS SUMMARY (3 total)
D/BaseAds-AdRegistry:   ✅ Ready: 1
D/BaseAds-AdRegistry:   ⏳ Initializing: 1
D/BaseAds-AdRegistry:   ❌ Failed: 1
```

**Clear benefits:**
- ✅ Biết chính xác ad nào đang READY
- ✅ Biết ad nào đang init
- ✅ Biết ad nào bị fail và lý do
- ✅ Visual summary dễ đọc
- ✅ Track age của mỗi ad

---

## 🔗 API Usage

### **Get Ad Status Programmatically:**

```kotlin
// Get status of specific ad
val status = AdsLogger.getAdStatus("banner_1")
if (status == AdStatus.READY) {
    // Ad is ready to show
}

// Get summary of all ads
val summary = AdsLogger.getAdsSummary()
val readyCount = summary["ready_count"] as Int
val failedCount = summary["failed_count"] as Int
```

---

## 📚 Related Files

- `base-ads/src/main/java/com/tinhtx/baseads/core/AdsLogger.kt` - Enhanced logger implementation
- `base-ads/src/main/java/com/tinhtx/baseads/banner/BannerAdItem.kt` - Banner with status tracking
- `base-ads/src/main/java/com/tinhtx/baseads/interstitial/InterstitialAdManager.kt` - Interstitial with status tracking

---

**Test ngay để xem enhanced logs!** 🚀

```bash
adb logcat | grep "BaseAds-AdRegistry"
```
