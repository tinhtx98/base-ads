# 🎯 Interstitial Ads Logic & Configuration

## 📊 Current Configuration (Updated)

### **⏱️ Timing Constants:**

| Setting | Value | Description |
|---------|-------|-------------|
| **Cooldown** | `15s` | Thời gian tối thiểu giữa 2 lần show |
| **Daily Cap** | `15 lần` | Số lần tối đa show trong 1 ngày |
| **First Launch Delay** | `20s` | Delay sau khi mở app lần đầu |
| **Screen Open Delay** | `5s` | Delay sau khi mở màn hình mới |

---

## 🔄 Interstitial Ad Show Logic

### **Điều Kiện Bắt Buộc (Tất cả phải đạt):**

#### ✅ **1. User Settings:**
```kotlin
✓ User KHÔNG phải VIP
✓ Interstitial ads được ENABLE trong config
```

#### ✅ **2. Route Check:**
```kotlin
✓ Route hiện tại KHÔNG nằm trong blocklist
  (VD: "settings" bị block theo mặc định)
```

#### ✅ **3. Timing - Screen Open Delay:**
```kotlin
✓ Đã qua ít nhất 5 giây từ khi màn hình được mở
  (Tránh interrupt ngay khi user vừa vào màn hình)
```

#### ✅ **4. Timing - Cooldown Between Ads:**
```kotlin
✓ Đã qua ít nhất 15 giây từ lần show ad trước
  (Tránh spam ads liên tục)
```

#### ✅ **5. Daily Limit:**
```kotlin
✓ Chưa vượt quá 15 lần show trong ngày
  (Reset vào 00:00 mỗi ngày)
```

#### ✅ **6. Timing - First Launch:**
```kotlin
✓ Đã qua ít nhất 20 giây từ khi app được launch
  (Cho user làm quen với app trước)
```

#### ✅ **7. Ad Availability:**
```kotlin
✓ Có interstitial ad đã được preload sẵn
  (Ad phải load thành công trước)
```

---

## ⏰ Timeline Examples

### **🎬 Scenario 1: Lần Show Đầu Tiên**

```
t = 0s     │ App Launch
           │ ↓ preload() được gọi
           │ ↓ Ad bắt đầu load...
           │
t = 2s     │ Ad load xong → Status: READY ✅
           │
t = 10s    │ User mở màn hình "Home"
           │ ↓ markScreenOpened() được gọi
           │
t = 15s    │ User click button "Go to Details"
           │ ↓ Check: 15s > 20s? ❌ NO
           │ → Policy Failed: "First launch delay not met"
           │
t = 21s    │ User click button "Go to Settings"
           │ ↓ Check: 21s > 20s? ✅ YES
           │ ↓ Check: Time since screen open? 11s > 5s ✅
           │ ↓ Check: Route blocked? settings = BLOCKED ❌
           │ → Policy Failed: "Route 'settings' is blocked"
           │
t = 23s    │ User click button "Go to Details"
           │ ↓ Check ALL conditions...
           │ ✅ All passed!
           │ → SHOW INTERSTITIAL AD 🎬
```

**Result:** Ad show lần đầu sau **23 giây** từ khi launch app

---

### **🎬 Scenario 2: Show Ad Liên Tiếp**

```
t = 0s     │ Ad #1 dismissed
           │ ↓ Update lastInterstitialEpoch = now
           │ ↓ Increment today count (1/15)
           │ ↓ preload() next ad
           │
t = 10s    │ User click button
           │ ↓ Check: 10s > 15s cooldown? ❌ NO
           │ → Policy Failed: "Cooldown not met: 10s < 15s"
           │
t = 16s    │ User mở màn hình mới
           │ ↓ markScreenOpened()
           │
t = 18s    │ User click button
           │ ↓ Check: 18s > 15s cooldown? ✅ YES
           │ ↓ Check: 2s > 5s screen delay? ❌ NO
           │ → Policy Failed: "Not enough time since screen open"
           │
t = 22s    │ User click button
           │ ↓ Check ALL conditions...
           │ ✅ All passed!
           │ → SHOW INTERSTITIAL AD 🎬
```

**Result:** Ad show lần 2 sau **22 giây** từ lần show trước

---

### **🎬 Scenario 3: Daily Cap Reached**

```
Shows today: 14/15

t = 0s     │ Ad #14 dismissed
           │ ↓ Today count = 14/15
           │
t = 20s    │ User triggers ad
           │ → SHOW AD #15 🎬
           │ ↓ Today count = 15/15 (CAP REACHED)
           │
t = 40s    │ User triggers ad
           │ ↓ Check: 15 >= 15? ✅ CAP REACHED
           │ → Policy Failed: "Daily cap reached: 15 >= 15"
           │ ❌ NO MORE ADS TODAY
           │
NEXT DAY   │ 00:00 - Counter resets
           │ Today count = 0/15
           │ ✅ Can show ads again
```

---

## 🔢 Calculations

### **Minimum Time Between Ads:**
```
Cooldown (15s) + Screen Open Delay (5s) = 20 seconds
```

### **Maximum Ads Per Hour:**
```
3600s / 20s = 180 ads/hour (theoretical max)
But limited by Daily Cap = 15 ads/day
```

### **Average Time Per Ad (if hitting daily cap):**
```
Assuming 8 hours active use:
8 hours = 28,800 seconds
28,800s / 15 ads = 1,920 seconds = 32 minutes per ad
```

---

## 📝 Configuration Table

### **Current Settings:**

| Constant | Value | File Location |
|----------|-------|---------------|
| `INTERSTITIAL_COOLDOWN_SECONDS` | `15L` | `AdsConstants.kt` |
| `INTERSTITIAL_DAILY_CAP` | `15` | `AdsConstants.kt` |
| `INTERSTITIAL_FIRST_LAUNCH_DELAY_SECONDS` | `20L` | `AdsConstants.kt` |
| `MIN_SECONDS_AFTER_SCREEN_OPEN` | `5L` | `AdsConstants.kt` |

### **Previous Settings (Đã thay đổi):**

| Constant | Old Value | New Value | Change |
|----------|-----------|-----------|--------|
| `INTERSTITIAL_COOLDOWN_SECONDS` | `45L` | `15L` | ⬇️ **-66%** |
| `INTERSTITIAL_DAILY_CAP` | `12` | `15` | ⬆️ **+25%** |

---

## 🎯 User Experience Impact

### **Frequency Analysis:**

#### **Old Config (45s cooldown, 12 cap):**
- Minimum time between ads: **50s** (45s + 5s)
- Maximum per day: **12 ads**
- Max frequency: **1 ad per 50 seconds** (theoretical)
- Actual frequency: ~**1 ad per 40 minutes** (in real usage)

#### **New Config (15s cooldown, 15 cap):**
- Minimum time between ads: **20s** (15s + 5s)
- Maximum per day: **15 ads**
- Max frequency: **1 ad per 20 seconds** (theoretical)
- Actual frequency: ~**1 ad per 32 minutes** (in real usage)

### **Impact:**
- ✅ **More frequent ads** → More ad impressions → Higher revenue
- ⚠️ **Faster cooldown** → Need to ensure good UX
- ✅ **Higher daily cap** → Can show more ads in active use sessions
- ⚡ **Recommendation:** Monitor user retention metrics

---

## 🔧 How to Adjust Settings

### **File:** `base-ads/src/main/java/com/tinhtx/baseads/core/AdsConstants.kt`

```kotlin
object AdsConstants {
    
    // Thay đổi cooldown (giây)
    const val INTERSTITIAL_COOLDOWN_SECONDS = 15L  // ← Change this
    
    // Thay đổi daily cap (số lần)
    const val INTERSTITIAL_DAILY_CAP = 15  // ← Change this
    
    // Thay đổi first launch delay (giây)
    const val INTERSTITIAL_FIRST_LAUNCH_DELAY_SECONDS = 20L  // ← Change this
    
    // Thay đổi screen open delay (giây)
    const val MIN_SECONDS_AFTER_SCREEN_OPEN = 5L  // ← Change this
}
```

### **Suggested Ranges:**

| Setting | Min | Recommended | Max | Notes |
|---------|-----|-------------|-----|-------|
| Cooldown | 10s | 15-30s | 60s | < 10s = too aggressive |
| Daily Cap | 5 | 10-20 | 50 | > 50 = user fatigue |
| First Launch | 10s | 15-30s | 60s | Give users time to explore |
| Screen Open | 3s | 5s | 10s | Prevent immediate interrupt |

---

## 🐛 Debugging Ad Policy

### **Check Logs:**
```bash
adb logcat | grep "BaseAds-Policy"
```

### **Policy Check Failures:**

```
D/BaseAds-Policy: Ads disabled or VIP status
D/BaseAds-Policy: Route 'settings' is blocked
D/BaseAds-Policy: Not enough time since screen open: 2000ms < 5000ms
D/BaseAds-Policy: Cooldown not met: 10s < 15s
D/BaseAds-Policy: Daily cap reached: 15 >= 15
D/BaseAds-Policy: First launch delay not met: 5000ms < 20000ms
D/BaseAds-Policy: All policy checks passed ← ✅ Ad will show
```

### **Debug Info:**
```kotlin
val debugInfo = interstitialAdManager.getDebugInfo()
// Returns:
// - has_cached_ad: true/false
// - is_loading: true/false
// - last_screen_open_ms: timestamp
// - today_interstitial_count: 0-15
// - last_interstitial_epoch: timestamp
```

---

## 📊 Summary

### **✅ Updated Successfully:**
- Cooldown: `45s` → `15s` (⬇️ 66% faster)
- Daily Cap: `12` → `15` (⬆️ 25% more ads)

### **📈 Expected Impact:**
- **3x more frequent** ad opportunities (20s vs 50s)
- **25% more total ads** per day (15 vs 12)
- **Better monetization** potential
- **Still maintains** good UX with policies

### **⚠️ Monitor:**
- User retention rate
- Session duration
- Ad fill rate
- User feedback

---

**Build & Deploy:** ✅ Completed  
**Configuration:** ✅ Active  
**Ready to Test:** ✅ Yes  

**Test ad flow ngay để verify timing mới!** 🚀
