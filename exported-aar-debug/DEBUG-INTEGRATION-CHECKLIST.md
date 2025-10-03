# DEBUG Integration Checklist

## Pre-Integration Setup
- [ ] Remove any existing BaseAds AAR files
- [ ] Clean and rebuild your project
- [ ] Enable debug logging in your app

## File Setup
- [ ] Copy `base-ads-debug-v1.0.0-20251002_211827.aar` to `app/libs/`
- [ ] Add implementation line to `build.gradle.kts`
- [ ] Add all required dependencies
- [ ] Sync project

## Debug Configuration
- [ ] Set `AdsLogger.setLogLevel(AdsLogger.LogLevel.DEBUG)`
- [ ] Enable USB debugging on device
- [ ] Connect device and open Logcat
- [ ] Filter Logcat by "BaseAds"

## Test Basic Integration
- [ ] App compiles successfully
- [ ] No import errors for BaseAds classes
- [ ] Hilt integration works (`@Inject` annotations)
- [ ] Firebase is initialized

## Test Banner Ads
- [ ] Add banner preloader to your activity
- [ ] Check logs: "Preloading banner ad..."
- [ ] Verify banner appears on screen
- [ ] Check logs: "Banner ad loaded successfully"

## Test Interstitial Ads  
- [ ] Use `createSmartButtonOnClick()` on a button
- [ ] Click the button and check logs
- [ ] Should see: "Button clicked: [label]"
- [ ] Should see: "Post-button-click interstitial attempt - shown: true/false"
- [ ] Verify if config.showInterstitialBeforeNavigate is correct

## Test Smart Clickable
- [ ] Use `Modifier.smartButtonClick()` on a component
- [ ] Click and check for analytics events
- [ ] Verify ads show (if configured)

## Test Search Functions
- [ ] Use `createSmartSearchKeyboardActions()`
- [ ] Perform search and check logs
- [ ] Should see detailed search analytics

## Debug Common Issues

### No Ads Showing
- [ ] Check: `config.showInterstitialBeforeNavigate = false`
- [ ] Check: All required parameters passed (ads, config, activity)
- [ ] Check: `ads.isReady()` returns true
- [ ] Check: User is not VIP
- [ ] Check: Internet connection

### No Analytics
- [ ] Check Firebase initialization
- [ ] Check analytics logger is injected
- [ ] Check event names in Logcat
- [ ] Verify Firebase project setup

### Compilation Errors
- [ ] All dependencies added
- [ ] Proper Hilt setup
- [ ] Kotlin version compatibility
- [ ] Android SDK version compatibility

## Log Examples to Look For

### ✅ Success Logs
```
D/BaseAds-Initializer: Ads initialization completed
D/BaseAds-Banner: Banner ad loaded successfully  
D/BaseAds-Interstitial: Showing interstitial ad
D/BaseAds-Analytics: Event logged: button_click
```

### ⚠️ Warning Logs  
```
W/BaseAds-Interstitial: Interstitial not ready, skipping
W/BaseAds-Config: showInterstitialBeforeNavigate is true, ads disabled
```

### ❌ Error Logs
```
E/BaseAds-Ads: Failed to load ad: No internet connection
E/BaseAds-Firebase: Analytics not initialized
E/BaseAds-Hilt: Injection failed for component
```

## Final Verification
- [ ] All core features working
- [ ] Logs are clean (no unexpected errors)
- [ ] Analytics events firing correctly  
- [ ] Ready to switch to release AAR

---

**Need help?** Check the logs first - they contain detailed information about what's happening! 🐛
