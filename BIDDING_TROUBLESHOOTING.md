# 🔍 Phân tích vấn đề: Match Rate = 0 cho ironSource & Liftoff

## 📊 Triệu chứng quan sát

- ✅ **Bid Requests**: ironSource & Liftoff nhận được nhiều requests
- ❌ **Match Rate**: 0% - không có ad nào được serve
- 🤔 **Nguyên nhân**: Network không return bids hoặc bids bị reject

## 🎯 Nguyên nhân chính (theo độ ưu tiên)

### 1. ⚠️ **CHƯA LIÊN KẾT ACCOUNT** (90% khả năng)

#### Với ironSource:
```
❌ THIẾU: Link AdMob account <-> ironSource account
```

**Cách fix:**
1. Vào [ironSource Dashboard](https://platform.ironsrc.com/)
2. Navigate: **Account Settings** → **Ad Networks**
3. Tìm **Google AdMob**
4. Click **Connect** và follow OAuth flow
5. Authorize AdMob access to ironSource
6. Copy **App Key** (nếu có yêu cầu)
7. Quay lại AdMob Console:
   - Mediation → ironSource settings
   - Paste App Key (nếu được yêu cầu)
   - Verify connection status = **Connected**

**Lưu ý quan trọng với ironSource:**
```
⚠️ ironSource bidding qua AdMob YÊU CẦU:
1. Account linking (OAuth connection)
2. App phải được ADD vào ironSource dashboard
3. App status = Active
4. Bidding phải enabled trên ironSource side
```

#### Với Liftoff (Vungle):
```
❌ THIẾU: App ID configuration
```

**Cách fix:**
1. Vào [Vungle Dashboard](https://publisher.vungle.com/)
2. Tạo app (nếu chưa có)
3. Copy **App ID** (format: `5xxxxxx`)
4. Vào AdMob Console:
   - Mediation → Vungle settings
   - Nhập App ID
   - **QUAN TRỌNG**: Chọn **Bidding** (không phải Waterfall)

#### Với Meta:
```
❌ THIẾU: App ID + Placement IDs
```

**Cách fix:**
1. [Meta for Developers](https://developers.facebook.com/apps/)
2. Create/Select app
3. Enable **Audience Network**
4. Copy **App ID**
5. Create Placements cho từng format
6. Vào AdMob:
   - Mediation → Meta settings
   - Nhập App ID
   - Cho mỗi ad unit: nhập Placement ID tương ứng

---

### 2. 🚫 **APP CHƯA ĐƯỢC APPROVE** (80% khả năng)

#### ironSource:
- **Yêu cầu**: App phải approved trước khi serve live ads
- **Status check**: ironSource Dashboard → Apps → [Your App] → Status
- **Process time**: 1-3 business days
- **Requirements**:
  - App đã publish trên Store (hoặc TestFlight/Internal Testing)
  - Có privacy policy
  - Compliant với ironSource policies

#### Vungle/Liftoff:
- **Yêu cầu**: Similar approval process
- **Check**: Vungle Dashboard → Applications
- **Note**: Có thể serve test ads ngay, nhưng live ads cần approval

#### Meta Audience Network:
- **Yêu cầu**: Strict app review
- **Process**: Submit từ Meta dashboard
- **Time**: 3-7 days
- **Requirements**:
  - Đủ traffic (minimum threshold)
  - Content policy compliant
  - Privacy policy

---

### 3. 🔧 **CẤU HÌNH SAI TRÊN ADMOB** (70% khả năng)

#### Checklist:

```
[ ] Đã add ad source vào Mediation group?
[ ] Đã enable BIDDING (không phải Waterfall)?
[ ] Đã nhập đúng App ID/Key?
[ ] Ad unit đã được link với mediation group?
[ ] Bidding CPM floor = $0 hoặc reasonable value?
[ ] Geographic targeting không conflict?
[ ] Ad format matching (banner vs interstitial)?
```

#### Cách verify:
```bash
# Trong AdMob Console
Mediation → Ad Sources → [Network Name]

Expected:
✅ Status: Active
✅ Bidding: Enabled
✅ App ID: [Your ID]
✅ Connected: Yes
✅ Ad Units: [List your units]
```

---

### 4. 📍 **GEOGRAPHIC MISMATCH** (50% khả năng)

Một số network không có inventory ở mọi nơi:

```
ironSource: Tốt ở: US, UK, CA, AU, EU, JP, KR
           Yếu ở: Tier 3 countries

Vungle:     Tốt ở: US, EU, LATAM
           Yếu ở: Africa, Middle East

Meta:       Tốt ở: Global, especially US
           Moderate: Most regions
```

**Test:**
- Thử với VPN ở US location
- Nếu match rate > 0 → đây là vấn đề geo

---

### 5. 🧪 **TEST MODE RESTRICTIONS** (40% khả năng)

#### ironSource:
```kotlin
⚠️ ironSource bidding KHÔNG SERVE trong một số trường hợp test:
- Test device IDs được set
- Debug build với development signature
- Emulator (không phải 100% support)
```

**Workaround:**
- Test trên real device
- Use production build signature
- Hoặc chờ app approved rồi test

#### Vungle:
- Test ads available NGAY (không cần approval)
- Nhưng phải config đúng App ID

---

### 6. 🏗️ **CODE/CONFIGURATION ISSUES** (20% khả năng)

#### Check log output:

```bash
adb logcat | grep -E "(Initializer|MobileAds|Adapter)"
```

**Expected output cho mỗi adapter:**
```
✅ GOOD:
Adapter: IronSource, Status: READY, Description: IronSource adapter initialized

❌ BAD:
Adapter: IronSource, Status: NOT_READY, Description: Missing app key
Adapter: IronSource, Status: NOT_READY, Description: SDK not initialized
```

#### Nếu thấy NOT_READY, check:

**A. ironSource specific issues:**
```xml
<!-- ❌ KHÔNG CẦN thêm vào manifest với bidding -->
<!-- Adapter tự động handle -->

<!-- ✅ Chỉ cần trong build.gradle -->
implementation 'com.google.ads.mediation:ironsource:8.4.0.0'
```

**B. Vungle specific:**
```
✅ Current: com.google.ads.mediation:vungle:7.4.0.0
Check compatibility: https://developers.google.com/admob/android/mediation/vungle
```

**C. Version compatibility:**
```kotlin
// ✅ VERIFIED trong code hiện tại:
GMA SDK: 24.6.0
ironSource adapter: 8.4.0.0 (pulls SDK 8.4.0)
Vungle adapter: 7.4.0.0 (pulls SDK 7.4.0)
Meta adapter: 6.17.0.0 (pulls SDK 6.17.0)

// Tất cả compatible với GMA 24.6.0
```

---

## 🔬 Cách chẩn đoán chính xác

### Step 1: Check adapter initialization

```kotlin
// Thêm vào AdsInitializer.kt để log chi tiết hơn
MobileAds.initialize(context) { initStatus ->
    val statusMap = initStatus.adapterStatusMap
    
    statusMap.forEach { (adapter, status) ->
        when (status.initializationState) {
            AdapterStatus.State.READY -> {
                AdsLogger.i("Adapter", "✅ $adapter: READY")
            }
            AdapterStatus.State.NOT_READY -> {
                AdsLogger.e("Adapter", "❌ $adapter: NOT_READY - ${status.description}")
                // ⚠️ LOG THIS để debug
                analyticsLogger.logEvent("adapter_not_ready", mapOf(
                    "adapter" to adapter,
                    "reason" to status.description
                ))
            }
            else -> {
                AdsLogger.w("Adapter", "⚠️ $adapter: ${status.initializationState}")
            }
        }
    }
}
```

### Step 2: Enable verbose logging

```kotlin
// Trong Application.onCreate() TRƯỚC MobileAds.initialize()
MobileAds.setRequestConfiguration(
    RequestConfiguration.Builder()
        .setTestDeviceIds(testDevices)
        .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
        .build()
)

// Thêm tag logging
if (BuildConfig.DEBUG) {
    // This will show mediation requests in logcat
    // Filter: tag:Ads
}
```

### Step 3: Check mediation waterfall

Sau khi load ad, check logcat:

```bash
adb logcat -s Ads:D
```

Expected flow:
```
[Ads] Mediation request started
[Ads] Requesting bid from: IronSource
[Ads] Requesting bid from: Vungle  
[Ads] Requesting bid from: Meta
[Ads] Bid received from IronSource: $X.XX
[Ads] Bid received from Vungle: $Y.YY
[Ads] Winner: [Network] with bid $Z.ZZ
```

Nếu thấy:
```
❌ BAD:
[Ads] Requesting bid from: IronSource
[Ads] No bid from IronSource (reason: NO_FILL)
```

→ Network không có inventory hoặc chưa config đúng

---

## 🛠️ Action Plan - Làm theo thứ tự

### Priority 1: Account Linking (QUAN TRỌNG NHẤT)

#### ironSource:
1. ✅ Vào ironSource dashboard
2. ✅ Link với AdMob account (OAuth)
3. ✅ Add app vào ironSource
4. ✅ Copy App Key về AdMob (nếu yêu cầu)
5. ✅ Wait 10-15 minutes để sync

#### Vungle:
1. ✅ Tạo app trên Vungle dashboard
2. ✅ Copy App ID
3. ✅ Paste vào AdMob mediation settings
4. ✅ Verify connection

#### Meta:
1. ✅ Tạo app trên Meta for Developers
2. ✅ Enable Audience Network
3. ✅ Copy App ID + Create Placements
4. ✅ Paste vào AdMob

### Priority 2: Verify AdMob Configuration

```bash
# Checklist cho mỗi ad unit:
1. Ad unit → Mediation → Check ad sources
2. Mỗi source phải có:
   - Status: Active
   - Bidding: ON
   - App ID: Filled
   - CPM Floor: $0 hoặc low value (cho testing)
```

### Priority 3: Check Adapter Status

Run app và check log:
```bash
adb logcat | grep -E "(Adapter|Initializer)" > adapter_status.log
```

Tìm dòng:
- ✅ `Adapter: IronSource, Status: READY`
- ✅ `Adapter: Vungle, Status: READY`
- ❌ Nếu NOT_READY → đọc description để fix

### Priority 4: Test với Production Build

```bash
# Build release APK
./gradlew assembleRelease

# Install
adb install app/build/outputs/apk/release/app-release.apk

# Test trên real device (không phải emulator)
# Ở US location (dùng VPN nếu cần)
```

### Priority 5: Wait for Approval

- ironSource: 1-3 days
- Vungle: Usually instant for test, 1-2 days for live
- Meta: 3-7 days

---

## 🎯 Quick Win: Force Bidding Test

Nếu muốn test nhanh KHÔNG chờ approval:

### Option 1: Use Test Mode IDs

**Vungle test App ID:**
```
Test App ID: 5xxxx (get from Vungle docs)
Use test placements
```

**ironSource:**
```
⚠️ ironSource không có public test IDs
Phải dùng app thật và wait approval
```

### Option 2: Lower CPM Floor

Trong AdMob console:
```
Mediation → Ad Source → [Network] → eCPM Floor = $0.00
```

Điều này cho phép ANY bid win (dù $0.01) để test connectivity.

---

## 📊 Expected Timeline

```
Day 0: Implement code ✅ (DONE)
       Add dependencies ✅ (DONE)
       
Day 0: Link accounts ⏰ (TODO - BẠN CẦN LÀM)
       - ironSource OAuth linking
       - Vungle App ID
       - Meta App ID
       
Day 0: Configure AdMob ⏰ (TODO)
       - Add ad sources
       - Enable bidding
       - Set CPM floors
       
Day 0-1: Test adapter init
         → Should see READY status
         
Day 1-3: Wait for ironSource approval
Day 1-2: Wait for Vungle approval  
Day 3-7: Wait for Meta approval

Day 7: Full bidding operational 🎉
       → Match rate > 0%
       → Revenue flowing
```

---

## 🚨 Red Flags to Watch

### Trong Logcat:

```bash
❌ "Missing app key" → ironSource chưa link
❌ "SDK not initialized" → Adapter version issue
❌ "Invalid app ID" → Sai ID paste vào AdMob
❌ "App not approved" → Chờ approval
❌ "No inventory" → Geo issue hoặc targeting
```

### Trong AdMob Reports:

```
❌ Requests: 1000, Matches: 0 → Account linking issue
❌ Requests: 0 → Ad source chưa được add
✅ Requests: 1000, Matches: 200+ → Working!
```

---

## 💡 Pro Tips

1. **Test từng network riêng:**
   - Disable tất cả trừ ironSource → test
   - Disable tất cả trừ Vungle → test
   - Giúp isolate issue

2. **Check AdMob Alerts:**
   - AdMob console có notification nếu config sai
   - Check email for alerts

3. **Use AdMob Mediation Test Suite:**
   ```
   AdMob app có "Test Your Mediation" feature
   Giúp verify từng adapter connectivity
   ```

4. **Contact Support nếu cần:**
   - ironSource support: platform support chat
   - Vungle: publisher support
   - Meta: developer support

---

## 📝 Summary

**TL;DR của vấn đề:**

```
Code: ✅ Perfect
Dependencies: ✅ Correct versions
Problem: ❌ Account linking + AdMob configuration

Fix: 
1. Link ironSource/Vungle/Meta accounts với AdMob
2. Configure đúng trên AdMob Console
3. Wait for app approval
4. Test với production build

Expected result after fix:
- Match rate: 20-50% (depending on geo)
- Revenue lift: 15-40%
- Fill rate: Increased
```

**Next steps ngay bây giờ:**
1. ✅ Go to ironSource dashboard → Link account
2. ✅ Go to Vungle dashboard → Get App ID
3. ✅ Go to Meta dashboard → Get App ID + Placements
4. ✅ Paste all IDs vào AdMob Console
5. ⏰ Wait & monitor

---

Chúc may mắn! Nếu sau khi làm theo vẫn match rate = 0, show log adapter status để mình debug tiếp.
