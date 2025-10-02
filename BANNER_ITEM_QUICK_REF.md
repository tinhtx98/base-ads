# 🚀 Quick Reference - Banner as Item

## ✅ Đã tạo components:

### 1. `BannerAdItem` - Simple banner với padding tùy chỉnh
```kotlin
BannerAdItem(
    adId = "banner_1",           // ⚠️ REQUIRED: Unique ID
    adUnitsProvider = adUnitsProvider,
    adsConfig = adsConfig,
    vipGate = vipGate,
    analyticsLogger = analyticsLogger,
    topPadding = 16.dp,          // Tùy chỉnh
    bottomPadding = 16.dp,       // Tùy chỉnh
    showBackground = false       // true = có background màu nhẹ
)
```

### 2. `BannerAdCard` - Banner styled như Card
```kotlin
BannerAdCard(
    adId = "banner_card_1",      // ⚠️ REQUIRED: Unique ID
    adUnitsProvider = adUnitsProvider,
    adsConfig = adsConfig,
    vipGate = vipGate,
    analyticsLogger = analyticsLogger
)
```

---

## ⚠️ CRITICAL: Prevent Flickering

**ALWAYS use stable keys in LazyColumn + unique adId:**

```kotlin
// ❌ WRONG - Will flicker on scroll
LazyColumn {
    items(list.size) { index ->
        BannerAdItem(...)
    }
}

// ✅ CORRECT - Stable rendering
LazyColumn {
    list.forEachIndexed { index, item ->
        item(key = "banner_$index") {
            BannerAdItem(adId = "banner_$index", ...)
        }
    }
}
```

---

## 📝 Usage Examples:

### In LazyColumn (List):
```kotlin
LazyColumn {
    itemList.forEachIndexed { index, item ->
        // Your item with stable key
        item(key = "content_$index") {
            MyItemContent(item)
        }
        
        // Banner every 5 items with stable key
        if ((index + 1) % 5 == 0) {
            val bannerNum = (index + 1) / 5
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

### In Column (Scrollable):
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    ContentSection1()
    
    // Banner as item
    BannerAdItem(
        adUnitsProvider = adUnitsProvider,
        adsConfig = adsConfig,
        vipGate = vipGate,
        analyticsLogger = analyticsLogger,
        topPadding = 16.dp,
        bottomPadding = 16.dp
    )
    
    ContentSection2()
}
```

---

## 🎯 Test ngay:

1. **Run app** trên device
2. **Tap button** "List with Banner Items" ở Home screen
3. **Scroll list** → Sẽ thấy banner xuất hiện mỗi 5 items

---

## 📋 Files created:

- `base-ads/src/main/java/com/tinhtx/baseads/banner/BannerAdItem.kt` - Components
- `app/src/main/java/com/tinhtx/baseads/MainActivity.kt` - Example screen (ListWithBannersScreen)
- `BANNER_AS_ITEM_GUIDE.md` - Chi tiết guide

---

## 🎊 Key Features:

✅ Tự động fill full width  
✅ VIP gate integrated  
✅ Analytics tracking  
✅ Adaptive sizing  
✅ Custom padding  
✅ Optional background  
✅ Card styling option  

**Đơn giản chỉ cần add vào list/column như một item bình thường!**
