# BaseAds DEBUG Package Contents

## 📦 Files Included

```
exported-aar-debug/
├── base-ads-debug-v1.0.0-20251002_211827.aar                    # Main DEBUG AAR file
├── DEBUG-README.md                # Debug integration guide
├── DEBUG-INTEGRATION-CHECKLIST.md # Step-by-step checklist
├── PACKAGE-CONTENTS.md            # This file
└── prompts/                       # AI assistance prompts
    ├── debug-setup-prompt.md     # Setup help prompt
    └── debug-troubleshooting-prompt.md # Issue resolution prompt
```

## 🎯 Quick Start

1. **Copy AAR:** `base-ads-debug-v1.0.0-20251002_211827.aar` → `app/libs/`
2. **Add dependency:** `implementation(files("libs/base-ads-debug-v1.0.0-20251002_211827.aar"))`
3. **Enable logging:** `AdsLogger.setLogLevel(AdsLogger.LogLevel.DEBUG)`
4. **Filter Logcat:** Search for "BaseAds"
5. **Start debugging!** 🐛

## 🐛 Debug Features

- **Full logging enabled** - See everything that happens
- **Debug symbols included** - Better crash reports  
- **No obfuscation** - Readable stack traces
- **Analytics tracking** - Every event logged
- **Error details** - Comprehensive error information

## 📱 Testing Focus Areas

1. **Banner Preloading** - Check immediate display
2. **Interstitial Timing** - Verify post-click ads
3. **Search Analytics** - Test keyboard/button searches
4. **Smart Clickable** - Verify all interactions tracked
5. **Config Behavior** - Test showInterstitialBeforeNavigate
6. **VIP Handling** - Verify ad-free experience
7. **Error Recovery** - Test offline/error scenarios

## 🔍 Key Log Patterns

### Banner Logs
```
D/BaseAds-BannerPreloader: Starting preload...
D/BaseAds-BannerPreloader: Preload completed
D/BaseAds-AdaptiveBanner: Using preloaded ad
```

### Interstitial Logs  
```
D/BaseAds-Interstitial: maybeShow called
D/BaseAds-Interstitial: Showing interstitial ad
```

### Analytics Logs
```
D/BaseAds-Analytics: Event logged: button_click
D/BaseAds-Analytics: Event logged: search_performed
```

## ⚠️ Remember

- **DEBUG ONLY** - Don't use in production
- **Performance impact** - Debug logging has overhead
- **Large file size** - Includes debug symbols
- **Switch to release** when ready for production

---

**Happy debugging!** 🐛✨
