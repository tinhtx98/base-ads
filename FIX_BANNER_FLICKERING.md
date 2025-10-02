# 🔧 Fix: Banner Flickering in LazyColumn

## 🐛 Problem
Khi scroll trong LazyColumn với banner ads, banner bị **nháy/flickering** và **không phải lúc nào cũng hiển thị ads**.

### Root Causes:
1. **LazyColumn recomposition** - Mỗi lần scroll, items bị recompose
2. **AdView recreation** - AndroidView tạo AdView mới mỗi lần recompose
3. **No stable keys** - LazyColumn không biết item nào là item nào
4. **Ad reload** - Mỗi lần recompose, ad bị load lại từ đầu

---

## ✅ Solution Implemented

### 1. **Stable Keys in LazyColumn**

```kotlin
// ❌ BEFORE - No keys
LazyColumn {
    items(itemList.size) { index ->
        MyContent(itemList[index])
        
        if ((index + 1) % 5 == 0) {
            BannerAdItem(...)  // Will recreate on scroll!
        }
    }
}

// ✅ AFTER - With stable keys
LazyColumn {
    itemList.forEachIndexed { index, item ->
        item(key = "content_$index") {
            MyContent(item)
        }
        
        if ((index + 1) % 5 == 0) {
            val bannerNumber = (index + 1) / 5
            item(key = "banner_$bannerNumber") {
                BannerAdItem(
                    adId = "banner_$bannerNumber",
                    ...
                )
            }
        }
    }
}
```

### 2. **AdView State Preservation**

```kotlin
// In BannerAdItem.kt
@Composable
fun BannerAdItem(
    adId: String = "banner_ad",  // NEW: Unique ID parameter
    ...
) {
    // Remember height based on adId - prevents reset on recompose
    var bannerHeight by remember(adId) { mutableStateOf(60.dp) }
    
    AndroidView(
        factory = { ctx ->
            // Only created once per unique adId
            AdsLogger.d("BannerAdItem", "Creating AdView for key: $adId")
            AdView(ctx).apply {
                // Setup and load ad
                loadAd(adRequest)
            }
        },
        update = { adView ->
            // Don't reload on update to prevent flickering
            AdsLogger.d("BannerAdItem", "[$adId] Update called (no action)")
        }
    )
}
```

### 3. **DisposableEffect with Key**

```kotlin
DisposableEffect(adId) {
    AdsLogger.d("BannerAdItem", "[$adId] Composed")
    
    onDispose {
        AdsLogger.d("BannerAdItem", "[$adId] Disposed")
    }
}
```

---

## 📋 Implementation Steps

### Step 1: Update BannerAdItem Component

File: `base-ads/src/main/java/com/tinhtx/baseads/banner/BannerAdItem.kt`

**Changes:**
- ✅ Added `adId: String` parameter for unique identification
- ✅ `remember(adId)` ensures state is tied to specific banner instance
- ✅ `update` block does nothing to prevent reloading
- ✅ `DisposableEffect(adId)` for proper lifecycle tracking
- ✅ Enhanced logging with `[$adId]` tags

### Step 2: Update Usage in LazyColumn

File: `app/src/main/java/com/tinhtx/baseads/MainActivity.kt`

**Changes:**
```kotlin
LazyColumn {
    itemList.forEachIndexed { index, item ->
        // Regular item with stable key
        item(key = "content_$index") {
            MyContent(item)
        }
        
        // Banner with stable key
        if ((index + 1) % 5 == 0) {
            val bannerNumber = (index + 1) / 5
            item(key = "banner_$bannerNumber") {  // ✅ Stable key
                BannerAdItem(
                    adId = "banner_$bannerNumber",  // ✅ Unique ID
                    adUnitsProvider = adUnitsProvider,
                    adsConfig = adsConfig,
                    vipGate = vipGate,
                    analyticsLogger = analyticsLogger
                )
            }
        }
    }
}
```

---

## 🎯 How It Works

### **Without Keys (Old - Flickering):**
```
Scroll down → LazyColumn recomposes → 
All items recreated → Banner AdView recreated → 
Ad loads again → Blank while loading → Flicker!
```

### **With Keys (New - Stable):**
```
Scroll down → LazyColumn recomposes → 
Check keys → "banner_1" already exists → 
Reuse existing AdView → No reload → 
Smooth display! ✅
```

---

## 📊 Before vs After

### Before (Flickering):
- ❌ Banner nháy khi scroll
- ❌ Ads không load đều
- ❌ Poor user experience
- ❌ Multiple unnecessary ad requests

### After (Stable):
- ✅ Banner ổn định khi scroll
- ✅ Ads hiển thị consistent
- ✅ Smooth scrolling
- ✅ Ad request chỉ 1 lần per banner

---

## 🔍 Debugging

### Check Logs:
```bash
adb logcat | grep "BannerAdItem"
```

### Expected Output (No Flickering):
```
D/BannerAdItem: Creating AdView for key: banner_1
D/BannerAdItem: [banner_1] Size: 392x50dp
D/BannerAdItem: [banner_1] Ad request sent
D/BannerAdItem: [banner_1] Composed
D/BannerAdItem: [banner_1] Ad loaded
# Scroll down...
D/BannerAdItem: [banner_1] Update called (no action)  ← No reload!
# Scroll up...
D/BannerAdItem: [banner_1] Update called (no action)  ← Still stable!
```

### Bad Output (Flickering - if not fixed):
```
D/BannerAdItem: Creating AdView for key: banner_1
D/BannerAdItem: [banner_1] Ad loaded
# Scroll...
D/BannerAdItem: Creating AdView for key: banner_1  ← RECREATED!
D/BannerAdItem: [banner_1] Ad loaded  ← RELOADED!
```

---

## 💡 Best Practices

### 1. **Always Use Stable Keys**
```kotlin
// ❌ BAD
LazyColumn {
    items(list.size) { index -> /* No key */ }
}

// ✅ GOOD
LazyColumn {
    list.forEachIndexed { index, item ->
        item(key = "item_$index") { /* Stable key */ }
    }
}
```

### 2. **Unique Banner IDs**
```kotlin
// ❌ BAD - Same ID for all banners
BannerAdItem(adId = "banner")

// ✅ GOOD - Unique ID per banner
BannerAdItem(adId = "banner_$bannerNumber")
```

### 3. **Key-Based State**
```kotlin
// ❌ BAD - State not tied to key
var height by remember { mutableStateOf(60.dp) }

// ✅ GOOD - State remembers based on key
var height by remember(adId) { mutableStateOf(60.dp) }
```

### 4. **Prevent Reloading in Update**
```kotlin
AndroidView(
    factory = { /* Create AdView */ },
    update = { adView ->
        // ❌ BAD - Don't reload here
        // adView.loadAd(newRequest)
        
        // ✅ GOOD - No action to prevent reload
    }
)
```

---

## 🎊 Result

**Banners giờ:**
- ✅ Hiển thị smooth khi scroll
- ✅ Không bị nháy/flicker
- ✅ Load đều và consistent
- ✅ Better performance (ít ad requests hơn)
- ✅ Better user experience

---

## 📝 Usage Template

```kotlin
LazyColumn {
    dataList.forEachIndexed { index, item ->
        // Content item
        item(key = "content_$index") {
            YourItemComposable(item)
        }
        
        // Banner every N items
        if ((index + 1) % N == 0) {
            val bannerNum = (index + 1) / N
            item(key = "banner_$bannerNum") {
                BannerAdItem(
                    adId = "banner_$bannerNum",
                    adUnitsProvider = adUnitsProvider,
                    adsConfig = adsConfig,
                    vipGate = vipGate,
                    analyticsLogger = analyticsLogger
                )
            }
        }
    }
}
```

---

## 🔗 Related Files

- `base-ads/src/main/java/com/tinhtx/baseads/banner/BannerAdItem.kt` - Component implementation
- `app/src/main/java/com/tinhtx/baseads/MainActivity.kt` - Example usage in `ListWithBannersScreen()`

**Test ngay để thấy sự khác biệt!** 🚀
