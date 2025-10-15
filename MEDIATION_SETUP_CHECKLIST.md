# ✅ Mediation Configuration Checklist

## 📋 Quick Diagnosis

Run này để check nhanh status:
```bash
./debug-mediation.sh
```

Expected output:
```
✅ Adapter: IronSource: READY
✅ Adapter: Vungle: READY  
✅ Adapter: Meta Audience Network: READY
```

Nếu thấy `NOT_READY`, làm theo hướng dẫn dưới.

---

## 🎯 Phase 1: Code & Dependencies (DONE ✅)

- [x] Google Mobile Ads SDK: 24.6.0
- [x] ironSource adapter: 8.4.0.0
- [x] Vungle adapter: 7.4.0.0
- [x] Meta adapter: 6.17.0.0
- [x] Analytics managers created
- [x] Initialization logic correct
- [x] No dependency conflicts

**Status:** ✅ Code is ready

---

## 🔧 Phase 2: AdMob Console Configuration (TODO)

### For ironSource:

#### Step 1: Link Account
- [ ] Go to [ironSource Dashboard](https://platform.ironsrc.com/)
- [ ] Navigate to: **Account Settings** → **Ad Networks** → **Google AdMob**
- [ ] Click **Connect** button
- [ ] Complete OAuth authorization flow
- [ ] Verify status shows **Connected** with green checkmark

#### Step 2: Add App to ironSource
- [ ] In ironSource dashboard: **Apps** → **Add App**
- [ ] Enter your app details:
  - App name: `[Your App Name]`
  - Package name: `com.tinhtx.baseads` (match với app)
  - Platform: Android
- [ ] Copy **App Key** (format: `xxxxxxxx`)
- [ ] Save app

#### Step 3: Enable Bidding on ironSource
- [ ] Select your app in ironSource
- [ ] Go to **Setup** → **Bidding**
- [ ] Toggle **Enable Bidding**: ON
- [ ] Select ad formats: Banner ✅, Interstitial ✅
- [ ] Save changes

#### Step 4: Configure in AdMob
- [ ] Go to [AdMob Console](https://apps.admob.com/)
- [ ] Navigate: **Mediation** → **Ad Sources**
- [ ] Find **ironSource** or click **Add Ad Source**
- [ ] In settings:
  - Connection status: Should show **Connected**
  - App Key: `[Paste from ironSource]`
  - Bidding: **ON** (not Waterfall)
- [ ] Click **Save**

#### Step 5: Add to Ad Units
For **each** ad unit (banner, interstitial):
- [ ] Open ad unit settings
- [ ] Go to **Mediation** tab
- [ ] Click **Add ad source**
- [ ] Select **ironSource**
- [ ] Verify:
  - Status: **Active**
  - Bidding: **Enabled**
  - eCPM Floor: `$0.00` (for testing)
- [ ] Click **Save**

#### Step 6: Wait & Verify
- [ ] Wait 10-15 minutes for sync
- [ ] Run app: `./gradlew installDebug`
- [ ] Check log: `./debug-mediation.sh`
- [ ] Should see: `✅ Adapter: IronSource: READY`

---

### For Vungle (Liftoff):

#### Step 1: Get App ID
- [ ] Go to [Vungle Dashboard](https://publisher.vungle.com/)
- [ ] Create/Select app
- [ ] Navigate: **Applications**
- [ ] Copy **App ID** (format: `5xxxxxxxx`)

#### Step 2: Create Placements
- [ ] In Vungle dashboard: **Monetization** → **Placements**
- [ ] Create Banner placement:
  - Name: "Banner Main"
  - Format: Banner
  - Copy **Placement ID**
- [ ] Create Interstitial placement:
  - Name: "Interstitial Main"
  - Format: Interstitial
  - Copy **Placement ID**

#### Step 3: Configure in AdMob
- [ ] Go to AdMob Console
- [ ] **Mediation** → **Ad Sources** → **Vungle**
- [ ] Enter:
  - App ID: `[Paste from Vungle]`
  - Bidding: **ON**
- [ ] Save

#### Step 4: Add to Ad Units
For banner ad unit:
- [ ] Open ad unit → **Mediation**
- [ ] Add **Vungle**
- [ ] Enter **Banner Placement ID**
- [ ] Bidding: ON, eCPM Floor: $0.00
- [ ] Save

For interstitial ad unit:
- [ ] Open ad unit → **Mediation**
- [ ] Add **Vungle**
- [ ] Enter **Interstitial Placement ID**
- [ ] Bidding: ON, eCPM Floor: $0.00
- [ ] Save

#### Step 5: Test
- [ ] Wait 5 minutes
- [ ] Run app & check logs
- [ ] Should see: `✅ Adapter: Vungle: READY`

---

### For Meta Audience Network:

#### Step 1: Create App
- [ ] Go to [Meta for Developers](https://developers.facebook.com/apps/)
- [ ] Create New App (or select existing)
- [ ] Add **Audience Network** product
- [ ] Copy **App ID** (16 digits)

#### Step 2: Create Placements
- [ ] In Meta dashboard: **Monetization** → **Placements**
- [ ] Create Banner:
  - Name: "Banner Main"
  - Format: Banner (320x50)
  - Copy **Placement ID**
- [ ] Create Interstitial:
  - Name: "Interstitial Main"
  - Format: Interstitial
  - Copy **Placement ID**

#### Step 3: Configure in AdMob
- [ ] AdMob Console → **Mediation** → **Meta Audience Network**
- [ ] Click **Connect**
- [ ] Authorize OAuth
- [ ] Enter App ID
- [ ] Bidding: **ON**
- [ ] Save

#### Step 4: Add to Ad Units
For banner:
- [ ] Ad unit → Mediation → Add **Meta**
- [ ] Enter Banner Placement ID
- [ ] Bidding: ON, eCPM: $0.00
- [ ] Save

For interstitial:
- [ ] Ad unit → Mediation → Add **Meta**
- [ ] Enter Interstitial Placement ID
- [ ] Bidding: ON, eCPM: $0.00
- [ ] Save

#### Step 5: Test
- [ ] Wait 5-10 minutes
- [ ] Run app
- [ ] Check: `✅ Adapter: Meta Audience Network: READY`

---

## 🧪 Phase 3: Testing

### Test 1: Adapter Status
```bash
# Run this
./debug-mediation.sh

# Expected:
═══════════════════════════════════════
📊 ADAPTER STATUS REPORT
═══════════════════════════════════════
✅ Google Mobile Ads SDK: READY (120ms)
✅ IronSource: READY (450ms)
✅ Vungle: READY (380ms)
✅ Meta Audience Network: READY (520ms)
═══════════════════════════════════════
Summary: 4 ready, 0 not ready
═══════════════════════════════════════
```

### Test 2: Load Banner
- [ ] Navigate to screen with banner
- [ ] Check log for mediation request:
```
[Ads] Mediation request started for banner
[Ads] Bid request sent to: IronSource, Vungle, Meta
[Ads] Bid received from IronSource: $0.15
[Ads] Bid received from Vungle: $0.12
[Ads] Bid received from Meta: $0.18
[Ads] Winner: Meta with bid $0.18
[Ads] Banner loaded from Meta Audience Network
```

### Test 3: Load Interstitial
- [ ] Trigger interstitial
- [ ] Check mediation flow in log
- [ ] Ad should display

### Test 4: Check AdMob Reports (after 24h)
- [ ] Go to AdMob Console → **Reports**
- [ ] Filter by **Ad Source**
- [ ] Verify:
  - ironSource: Requests > 0, Match rate > 0%
  - Vungle: Requests > 0, Match rate > 0%
  - Meta: Requests > 0, Match rate > 0%

---

## 🚨 Troubleshooting

### If Adapter Shows NOT_READY:

#### "Missing app key" (ironSource)
→ App Key chưa được nhập hoặc sai
→ Fix: Copy lại App Key từ ironSource dashboard

#### "SDK not initialized" (Any)
→ Adapter version incompatible
→ Fix: Check version trong `libs.versions.toml`

#### "Invalid app ID" (Vungle/Meta)
→ App ID sai format hoặc typo
→ Fix: Double-check ID, paste lại

#### "App not approved"
→ App chưa được network approve
→ Fix: Wait for approval (check network dashboard)

#### "No inventory" (During ad load)
→ Geographic mismatch hoặc no fill
→ Fix: Test with VPN ở US, hoặc wait for more inventory

---

## 📊 Success Metrics

After complete setup, you should see:

### In Logs:
```
✅ 4/4 adapters READY
✅ Bid requests sent to all networks
✅ Bids received from multiple networks
✅ Ads loading successfully
```

### In AdMob Reports (after 24-48h):
```
ironSource:
- Requests: 1,000+
- Matches: 200-400 (20-40%)
- eCPM: $0.50-$2.00

Vungle:
- Requests: 1,000+
- Matches: 150-350 (15-35%)
- eCPM: $0.40-$1.80

Meta:
- Requests: 1,000+
- Matches: 300-500 (30-50%)
- eCPM: $0.60-$2.50
```

### Revenue Impact:
- Fill rate: +15-25%
- eCPM: +20-40%
- Total revenue: +25-50%

---

## 📞 Support Contacts

### ironSource:
- Dashboard: https://platform.ironsrc.com/
- Support: platform@ironsrc.com
- Docs: https://developers.is.com/

### Vungle:
- Dashboard: https://publisher.vungle.com/
- Support: pub.support@vungle.com
- Docs: https://support.vungle.com/

### Meta:
- Dashboard: https://developers.facebook.com/
- Support: https://www.facebook.com/business/help
- Docs: https://developers.facebook.com/docs/audience-network

### AdMob:
- Console: https://apps.admob.com/
- Support: https://support.google.com/admob
- Docs: https://developers.google.com/admob

---

## 💡 Pro Tips

1. **Start with one network first**
   - Configure ironSource completely
   - Verify it works (match rate > 0)
   - Then add Vungle
   - Then add Meta
   - Easier to debug issues

2. **Use test placements**
   - Vungle & Meta have test placement IDs
   - Use those first to verify connectivity
   - Switch to real IDs when ready

3. **Monitor daily**
   - Check AdMob reports every day
   - Compare performance across networks
   - Adjust eCPM floors if needed

4. **Geographic optimization**
   - Different networks perform better in different geos
   - Use AdMob's geo-targeting to optimize
   - Example: Meta strong in US, ironSource in EU

5. **A/B testing**
   - Test with/without each network
   - Measure revenue impact
   - Keep the winners

---

## ✅ Final Checklist

Before declaring "DONE":

- [ ] All 3 networks show READY in logs
- [ ] Ads loading from multiple networks (check logs)
- [ ] No errors in logcat
- [ ] AdMob reports show matches > 0 for all networks
- [ ] Revenue increasing compared to baseline
- [ ] No crashes related to ads
- [ ] Privacy compliance (UMP) implemented
- [ ] Production tested on real devices
- [ ] Monitoring set up (Firebase Analytics)

When all checked ✅ → **Mission Complete!** 🎉

---

## 📝 Notes

Ghi chú trong quá trình setup:

```
Date: _____________
ironSource App Key: ___________________________
Vungle App ID: ___________________________
Meta App ID: ___________________________

Issues encountered:
1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

Resolution:
1. _______________________________________________
2. _______________________________________________
3. _______________________________________________
```

---

**Next Action:** Go to AdMob Console and start configuration! 🚀
