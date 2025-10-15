# 🚀 Quick Fix Guide: Enable Production Mode

## ✅ ROOT CAUSE CONFIRMED

```
Problem:  eCPM = "-" for ironSource, Liftoff, Meta
Reason:   Test device mode prevents real bidding
Solution: Switch to production mode temporarily
```

---

## 🎯 Quick Fix (Manual - 2 phút)

### Step 1: Edit BaseAdsApplication.kt

Tìm dòng này (line ~44):
```kotlin
// MANUAL OVERRIDE for testing real bidding in debug:
// Uncomment below to test production bidding behavior:
// adsInitializer.initializeProductionMode(context = this)
```

**Uncomment để thành:**
```kotlin
// MANUAL OVERRIDE for testing real bidding in debug:
// Uncomment below to test production bidding behavior:
adsInitializer.initializeProductionMode(context = this)
```

### Step 2: Rebuild & Install
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Launch & Monitor
```bash
# Launch app
adb shell am start -n com.tinhtx.baseads/.MainActivity

# Check logs (should see "PRODUCTION MODE")
./debug-mediation.sh
```

Expected log:
```
🚀 PRODUCTION MODE: Real ads will be served from all networks!
⚠️ Bidding networks (ironSource, Vungle, Meta) will return real bids
```

### Step 4: Test Ads (Carefully!)
1. Load 3-5 banner ads
2. Load 2-3 interstitial ads
3. **DO NOT CLICK** (policy violation!)
4. Stop testing

### Step 5: Check Results (1-2 hours later)
Go to AdMob Console → Reports → Mediation:
```
BEFORE:
ironSource: eCPM: "-"
Liftoff:    eCPM: "-"
Meta:       eCPM: "-"

AFTER:
ironSource: eCPM: $0.60 ✅
Liftoff:    eCPM: $0.55 ✅
Meta:       eCPM: $0.75 ✅
```

### Step 6: Restore Test Mode
```bash
# Comment out the line again:
// adsInitializer.initializeProductionMode(context = this)

# Or use git:
git checkout app/src/main/java/com/tinhtx/baseads/BaseAdsApplication.kt
```

---

## 🤖 Automated Fix (Recommended)

Hoặc dùng script tự động:
```bash
./test-production-bidding.sh
```

Script sẽ:
1. ✅ Enable production mode
2. ✅ Build APK
3. ✅ Install
4. ✅ Monitor logs
5. ⚠️ Show warnings

---

## ⚠️ IMPORTANT WARNINGS

### DO NOT:
```
❌ Click on your own ads (policy violation)
❌ Test extensively in production mode (waste budget)
❌ Leave production mode on during development
❌ Refresh ads excessively
```

### DO:
```
✅ Test minimally (5-10 ad loads total)
✅ Wait for AdMob reports (1-2 hours)
✅ Switch back to test mode after verification
✅ Use production mode only in release builds
```

---

## 📊 Expected Timeline

```
Now:        Enable production mode
+5 mins:    Ads loading, bidding happening
+1 hour:    AdMob reports updating
+2 hours:   eCPM values appear ✅
+24 hours:  Stable match rates visible

ironSource: 20-30% match rate
Vungle:     15-25% match rate
Meta:       30-40% match rate
```

---

## 🎯 Verification Checklist

After enabling production mode:

### Immediate:
- [ ] Log shows "🚀 PRODUCTION MODE"
- [ ] All adapters READY
- [ ] Ads loading (may take few tries)

### 1-2 Hours Later:
- [ ] AdMob Reports updated
- [ ] ironSource eCPM has value (not "-")
- [ ] Liftoff eCPM has value (not "-")
- [ ] Meta eCPM has value (not "-")
- [ ] Match rates > 0%

### Success Criteria:
```
✅ eCPM values showing
✅ Match rates 15-40% per network
✅ Total match rate ~100%
✅ Revenue increased vs test mode
```

---

## 🔄 Long-term Setup

### For Development:
```kotlin
if (BuildConfig.DEBUG) {
    // Test mode: Safe for extensive testing
    adsInitializer.initializeWithTestDevices(this, true)
}
```

### For Release:
```kotlin
if (!BuildConfig.DEBUG) {
    // Production mode: Real bidding
    adsInitializer.initializeProductionMode(this)
}
```

**Current code already implements this!** ✅

Debug builds = Test mode (default)
Release builds = Production mode (automatic)

---

## 🐛 Troubleshooting

### Issue: Still eCPM = "-" after 2 hours

**Check:**
1. Did you actually enable production mode? (check logs)
2. Did you load enough ads? (need 5-10 minimum)
3. Are apps approved on network dashboards?
4. Try different geo with VPN (US recommended)

### Issue: Ads not loading at all

**Check:**
1. Adapter status (should be READY)
2. Network connectivity
3. Ad unit IDs correct?
4. Check logcat for errors

---

## 📞 Next Steps

1. ✅ Enable production mode (2 mins)
2. 🧪 Test ads (5 mins, 5-10 loads)
3. ⏰ Wait (1-2 hours)
4. 📊 Check AdMob Reports
5. 🎉 Celebrate when eCPM appears!
6. 🔄 Switch back to test mode

---

**Ready? Let's do it!** 🚀

Choose:
- **Manual**: Edit BaseAdsApplication.kt (Step 1 above)
- **Auto**: Run `./test-production-bidding.sh`
