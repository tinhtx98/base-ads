# 📱 Banner Ads as Items - Integration Guide

## 🎯 Tổng Quan

Module Base Ads cung cấp 2 composable components để dễ dàng thêm banner ads như một item trong list, column, hoặc bất kỳ scrollable content nào:

1. **`BannerAdItem`** - Simple banner với padding tùy chỉnh
2. **`BannerAdCard`** - Banner với card styling (elevation, padding)

---

## 🚀 Quick Start

### 1. Import Components

```kotlin
import com.tinhtx.baseads.banner.BannerAdItem
import com.tinhtx.baseads.banner.BannerAdCard
```

### 2. Basic Usage trong LazyColumn (WITH STABLE KEYS - IMPORTANT!)

```kotlin
@Composable
fun MyListScreen() {
    val items = listOf("Item 1", "Item 2", "Item 3", /*...*/)
    
    LazyColumn {
        items.forEachIndexed { index, item ->
            // Your regular item with stable key
            item(key = "content_$index") {
                Text(text = item)
            }
            
            // Insert banner every 5 items with stable key
            if ((index + 1) % 5 == 0) {
                val bannerNumber = (index + 1) / 5
                item(key = "banner_$bannerNumber") {
                    BannerAdItem(
                        adId = "banner_$bannerNumber",  // ← IMPORTANT: Unique ID
                        adUnitsProvider = adUnitsProvider,
                        adsConfig = adsConfig,
                        vipGate = vipGate,
                        analyticsLogger = analyticsLogger
                    )
                }
            }
        }
    }
}
```

**⚠️ CRITICAL: Always provide stable `key` in `item()` and unique `adId` to prevent flickering!**

### 3. Usage trong Column (Scrollable)

```kotlin
@Composable
fun MyScrollableScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Content 1
        Card { /* ... */ }
        
        // Banner as item
        BannerAdItem(
            adUnitsProvider = adUnitsProvider,
            adsConfig = adsConfig,
            vipGate = vipGate,
            analyticsLogger = analyticsLogger,
            topPadding = 16.dp,
            bottomPadding = 16.dp
        )
        
        // Content 2
        Card { /* ... */ }
        
        // More content...
    }
}
```

---

## 🎨 Component Options

### **BannerAdItem** - Flexible Banner

```kotlin
BannerAdItem(
    adId = "banner_1",                      // REQUIRED: Unique ID per banner
    adUnitsProvider = adUnitsProvider,
    adsConfig = adsConfig,
    vipGate = vipGate,
    analyticsLogger = analyticsLogger,
    modifier = Modifier,                    // Optional: custom modifier
    topPadding = 8.dp,                      // Optional: top padding
    bottomPadding = 8.dp,                   // Optional: bottom padding
    showBackground = false                  // Optional: subtle background
)
```

**Parameters:**
- `adId` - **REQUIRED** unique identifier (prevents flickering)
- `topPadding` - Space above banner (default: 8.dp)
- `bottomPadding` - Space below banner (default: 8.dp)
- `showBackground` - Show subtle background color (default: false)

**When to use:**
- In lists with consistent spacing
- When you want custom padding control
- For minimal visual styling

---

### **BannerAdCard** - Card-Styled Banner

```kotlin
BannerAdCard(
    adId = "banner_card_1",                 // REQUIRED: Unique ID per banner
    adUnitsProvider = adUnitsProvider,
    adsConfig = adsConfig,
    vipGate = vipGate,
    analyticsLogger = analyticsLogger,
    modifier = Modifier
)
```

**Features:**
- Automatic card elevation (2.dp)
- Consistent horizontal/vertical padding (16dp/8dp)
- Matches Material 3 card styling
- **Requires unique `adId` for stable rendering**

**When to use:**
- List với các card items khác
- Khi muốn banner có consistent look với UI
- For professional appearance

---

## 📋 Common Patterns

### Pattern 1: Banner Every N Items (WITH STABLE KEYS)

```kotlin
LazyColumn {
    itemList.forEachIndexed { index, item ->
        // Regular item with stable key
        item(key = "content_$index") {
            MyItemContent(item)
        }
        
        // Banner every 5 items with stable key
        if ((index + 1) % 5 == 0 && index < itemList.size - 1) {
            val bannerNumber = (index + 1) / 5
            item(key = "banner_$bannerNumber") {
                BannerAdItem(
                    adId = "banner_$bannerNumber",
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

### Pattern 2: Banner at Specific Positions (WITH STABLE KEYS)

```kotlin
LazyColumn {
    // Items before banner
    itemList.forEach { item ->
        item(key = "content_${item.id}") {
            MyItemContent(item)
        }
    }
    
    // Banner after items with stable key
    item(key = "banner_middle") {
        BannerAdCard(
            adId = "banner_middle",
            adUnitsProvider = adUnitsProvider,
            adsConfig = adsConfig,
            vipGate = vipGate,
            analyticsLogger = analyticsLogger
        )
    }
    
    // More items
    moreItems.forEach { item ->
        item(key = "more_content_${item.id}") {
            MoreContent(item)
        }
    }
}
```

### Pattern 3: Multiple Banners in ScrollColumn

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    // Section 1
    ContentSection1()
    
    // Banner 1
    BannerAdItem(
        adUnitsProvider = adUnitsProvider,
        adsConfig = adsConfig,
        vipGate = vipGate,
        analyticsLogger = analyticsLogger,
        topPadding = 16.dp,
        bottomPadding = 16.dp,
        showBackground = true
    )
    
    // Section 2
    ContentSection2()
    
    // Banner 2
    BannerAdCard(
        adUnitsProvider = adUnitsProvider,
        adsConfig = adsConfig,
        vipGate = vipGate,
        analyticsLogger = analyticsLogger
    )
    
    // Section 3
    ContentSection3()
}
```

### Pattern 4: Grid with Banners

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    modifier = Modifier.fillMaxSize()
) {
    items(gridItems) { item ->
        GridItemContent(item)
    }
    
    // Full-width banner spanning both columns
    item(span = { GridItemSpan(2) }) {
        BannerAdItem(
            adUnitsProvider = adUnitsProvider,
            adsConfig = adsConfig,
            vipGate = vipGate,
            analyticsLogger = analyticsLogger,
            topPadding = 16.dp,
            bottomPadding = 16.dp
        )
    }
    
    items(moreGridItems) { item ->
        GridItemContent(item)
    }
}
```

---

## ⚙️ Advanced Customization

### Custom Styling

```kotlin
// With background and custom spacing
BannerAdItem(
    adUnitsProvider = adUnitsProvider,
    adsConfig = adsConfig,
    vipGate = vipGate,
    analyticsLogger = analyticsLogger,
    modifier = Modifier.background(Color.LightGray),
    topPadding = 24.dp,
    bottomPadding = 24.dp,
    showBackground = true
)
```

### Conditional Banner Display

```kotlin
LazyColumn {
    items(itemList.size) { index ->
        MyItemContent(itemList[index])
        
        // Show banner only if not VIP
        if ((index + 1) % 5 == 0) {
            if (!vipGate.isVip()) {
                BannerAdItem(
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

## 🎯 Best Practices

### ✅ DO:

1. **Space banners appropriately** - Mỗi 5-10 items là reasonable
   ```kotlin
   if ((index + 1) % 5 == 0) { /* Insert banner */ }
   ```

2. **Use consistent banner type** - Stick to either BannerAdItem or BannerAdCard
   ```kotlin
   // All banners use same type
   BannerAdItem(...)
   ```

3. **Handle VIP users gracefully** - Banner tự động ẩn cho VIP
   ```kotlin
   // Component tự check VIP gate
   BannerAdItem(vipGate = vipGate, ...)
   ```

4. **Test on different screen sizes**
   ```kotlin
   // Banner tự adapt theo screen width
   ```

### ❌ DON'T:

1. **Too many banners** - Avoid banner mỗi 1-2 items
   ```kotlin
   // ❌ TOO FREQUENT
   if ((index + 1) % 2 == 0) { /* Too many ads! */ }
   
   // ✅ BETTER
   if ((index + 1) % 7 == 0) { /* More reasonable */ }
   ```

2. **Mix banner types randomly**
   ```kotlin
   // ❌ INCONSISTENT
   BannerAdItem(...)  // Item 5
   BannerAdCard(...)  // Item 10
   BannerAdItem(...)  // Item 15
   
   // ✅ CONSISTENT
   BannerAdCard(...)  // All cards
   ```

3. **Add manual width constraints**
   ```kotlin
   // ❌ WRONG - Banner needs full width
   BannerAdItem(modifier = Modifier.width(300.dp))
   
   // ✅ CORRECT
   BannerAdItem(modifier = Modifier.fillMaxWidth())
   ```

---

## 🐛 Troubleshooting

### Issue: Banner nháy/flickering khi scroll

**Root Cause:** Không có stable keys hoặc không có unique `adId`

**Solution:**
```kotlin
// ❌ WRONG - No keys, causes flickering
LazyColumn {
    items(list.size) { index ->
        BannerAdItem(...)  // Will recreate on scroll!
    }
}

// ✅ CORRECT - With stable keys
LazyColumn {
    list.forEachIndexed { index, item ->
        item(key = "banner_$index") {
            BannerAdItem(adId = "banner_$index", ...)
        }
    }
}
```

**See:** `FIX_BANNER_FLICKERING.md` for detailed explanation

### Issue: Banner không hiển thị

**Possible causes:**
1. VIP user (check `vipGate.isVip()`)
2. Ad not loaded yet
3. Test device không được config

**Solution:**
```kotlin
// Check logs
adb logcat | grep "BaseAds-Banner"
```

### Issue: Banner quá nhỏ hoặc quá lớn

**Solution:** Banner tự động adaptive, nhưng check parent constraints:
```kotlin
// ✅ Ensure parent is fillMaxWidth
Column(modifier = Modifier.fillMaxWidth()) {
    BannerAdItem(...)
}
```

### Issue: White space around banner

**Solution:** Adjust padding:
```kotlin
BannerAdItem(
    topPadding = 0.dp,
    bottomPadding = 0.dp,
    showBackground = false
)
```

---

## 📱 Complete Example

Check `MainActivity.kt` → `ListWithBannersScreen()` for full working example:

```kotlin
@Composable
fun ListWithBannersScreen() {
    val items = remember { (1..20).map { "Item #$it" } }
    
    LazyColumn {
        items(items.size) { index ->
            // Regular content card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = items[index])
            }
            
            // Banner every 5 items
            if ((index + 1) % 5 == 0 && index < items.size - 1) {
                BannerAdItem(
                    adUnitsProvider = adUnitsProvider,
                    adsConfig = adsConfig,
                    vipGate = vipGate,
                    analyticsLogger = analyticsLogger,
                    topPadding = 16.dp,
                    bottomPadding = 16.dp
                )
            }
        }
    }
}
```

---

## 🎊 Summary

| Component | Use Case | Styling |
|-----------|----------|---------|
| `BannerAdItem` | Lists, columns, flexible layouts | Minimal, custom padding |
| `BannerAdCard` | Card-based UI, professional look | Card elevation + padding |

Both components:
- ✅ Tự động adaptive sizing
- ✅ VIP gate integration
- ✅ Analytics tracking
- ✅ Fill full width
- ✅ No manual ad management needed

**Navigate to "List with Banner Items" trong sample app để xem live demo!**
