# Meta Audience Network Bidding Setup Guide

## ✅ Đã hoàn thành trong code

### 1. Dependencies đã thêm
- **Meta Audience Network adapter**: `com.google.ads.mediation:facebook:6.17.0.0`
- Adapter sẽ tự động pull Meta SDK cần thiết

### 2. Code Analytics đã chuẩn bị
- ✅ `MetaBiddingManager` để theo dõi analytics & performance
- ✅ Tích hợp vào `AdsInitializer` 
- ✅ Config flags: `enableMetaBidding`, `enableMetaLogging`
- ✅ Debug logging tương tự ironSource & Vungle

### 3. Đã dọn dẹp
- ❌ Xóa `admob = "23.5.0"` cũ (giữ lại `playServicesAds = "24.6.0"`)
- ❌ Xóa `implementation("com.ironsource.sdk:mediationsdk:8.3.0")` duplicate
- ✅ Chỉ dùng adapter chính thức từ Google

## 🔧 Cần làm trên AdMob Console

### Bước 1: Thêm Meta Audience Network vào Ad Unit

1. Đăng nhập [AdMob Console](https://apps.admob.com/)
2. Chọn **App** của bạn
3. Vào **Mediation** → chọn **Ad Sources**
4. Click **Add Ad Source** → chọn **Meta Audience Network**

### Bước 2: Kết nối Account

1. Click **Connect** để liên kết Meta account
2. Nếu chưa có Meta Audience Network account:
   - Tạo tại [Meta for Developers](https://developers.facebook.com/docs/audience-network)
   - Tạo App ID trên Meta Audience Network dashboard

### Bước 3: Cấu hình Bidding

1. Sau khi kết nối thành công, chọn **Enable Bidding** cho Meta
2. Nhập **Meta App ID** (lấy từ Meta Audience Network dashboard)
3. **Quan trọng**: Đảm bảo chọn **Bidding** (không phải Waterfall)

### Bước 4: Add vào từng Ad Unit

Cho **mỗi** ad unit (banner, interstitial):
1. Mở ad unit settings
2. Vào tab **Mediation**
3. Click **Add ad source**
4. Chọn **Meta Audience Network**
5. Enable **Bidding**
6. Nhập **Placement ID** (lấy từ Meta dashboard cho từng format)

### Bước 5: Test

1. Sync project trong Android Studio
2. Build & chạy app
3. Kiểm tra Logcat filter `MetaBidding`:
   ```
   MetaBidding: Initializing Meta Audience Network bidding analytics
   MetaBidding: Meta Audience Network bidding analytics initialized
   ```
4. Kiểm tra `AdsInitializer` log:
   ```
   Initializer: Adapter: Meta Audience Network, Status: READY
   ```

## 📱 Meta App & Placement IDs

### Lấy Meta App ID
1. Vào [Meta for Developers](https://developers.facebook.com/apps/)
2. Chọn app hoặc tạo mới
3. Vào **Audience Network** → **Settings**
4. Copy **App ID** (dạng: `1234567890123456`)

### Lấy Placement IDs
1. Trong Meta Audience Network dashboard
2. Vào **Monetization** → **Placements**
3. Tạo placement cho từng format:
   - **Banner**: Create Banner Placement
   - **Interstitial**: Create Interstitial Placement
4. Copy Placement ID (dạng: `1234567890123456_7890123456789012`)

## 🔍 Debug & Verify

### Kiểm tra adapter load
```kotlin
// Trong Logcat sau khi init
MobileAds: Adapter: Meta Audience Network
MobileAds: Status: READY / NOT_READY
MobileAds: Description: [chi tiết]
```

### Kiểm tra bidding events
```kotlin
// Analytics events được log:
- meta_bidding_init
- meta_bid_win (khi Meta thắng bid)
- meta_bid_loss (khi Meta thua bid)
- meta_bidding_metrics (performance metrics)
```

### Test Ads
1. **Test Mode**: Meta có test placement IDs riêng
   - Banner: `IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID`
   - Interstitial: `VID_HD_16_9_46S_APP_INSTALL#YOUR_PLACEMENT_ID`
2. Thêm test device hash vào Meta dashboard

## ⚠️ Lưu ý quan trọng

### 1. Privacy & Consent
- Meta yêu cầu user consent cho personalized ads (GDPR)
- Implement UMP SDK trước khi load ads trong EU/EEA

### 2. Meta App Review
- Sau khi test xong, submit app review trên Meta dashboard
- Cần approved mới show live ads trong production

### 3. Minimum SDK
- Meta Audience Network yêu cầu `minSdk >= 26` (bạn đã có)

### 4. Permissions
Đã có trong manifest (không cần thêm):
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
```

## 📊 Monitoring Performance

### Trong code (Analytics)
```kotlin
metaBiddingManager.logBiddingMetrics(
    adType = "banner",
    revenue = 0.05,
    winRate = 0.42
)
```

### Trên AdMob Console
1. **Mediation Reports**: Xem win rate, eCPM của Meta
2. **Compare**: So sánh performance Meta vs ironSource vs Vungle
3. **Optimization**: Adjust bidding floors nếu cần

### Trên Meta Dashboard
1. Xem fill rate, impressions
2. Estimated revenue từ Meta side
3. Reconcile với AdMob data

## 🚀 Production Checklist

- [ ] Meta App ID đã nhập đúng trong AdMob
- [ ] Placement IDs đã cấu hình cho tất cả ad units
- [ ] Bidding đã enabled (không phải waterfall)
- [ ] Test ads hoạt động
- [ ] Adapter status = READY trong log
- [ ] App đã approved trên Meta dashboard
- [ ] UMP consent flow implemented (nếu target EU)
- [ ] Test với live ads trên test device
- [ ] Analytics events tracking đúng

## 🔗 Tài liệu tham khảo

- [AdMob Meta Mediation Guide](https://developers.google.com/admob/android/mediation/meta)
- [Meta Audience Network Docs](https://developers.facebook.com/docs/audience-network)
- [Meta Bidding Integration](https://www.facebook.com/audiencenetwork/resources/blog/open-bidding-with-google-ad-manager)
- [Adapter Version Changelog](https://developers.google.com/admob/android/mediation/meta#changelog)

## 💡 Tips

1. **Bidding Floor**: Set minimum eCPM trên AdMob để filter low-value bids
2. **A/B Test**: Test với/không Meta để xem impact lên revenue
3. **Geographic**: Meta thường perform tốt ở Tier 1 countries (US, UK, CA, AU)
4. **Format**: Meta banner thường có fill rate cao hơn interstitial
5. **Frequency**: Monitor để tránh ad fatigue từ Meta

---

**Tóm tắt**: Code đã sẵn sàng. Chỉ cần cấu hình Meta App ID & Placement IDs trên AdMob UI, test, và submit Meta app review!
