# BaseAds DEBUG Troubleshooting Prompt

Use this when you're having issues with the DEBUG version:

---

**Issue:** [Describe your specific problem]

**BaseAds DEBUG Library:**
- File: base-ads-debug-v1.0.0-20251002_211827.aar
- Version: 1.0.0
- Type: DEBUG with full logging

**What I'm trying to do:**
[Explain what you're trying to achieve]

**Current behavior:**
[What's actually happening]

**Expected behavior:**
[What should happen]

**Debug logs:**
Filter Logcat by "BaseAds" and paste relevant logs:
```
[Paste BaseAds debug logs here]
```

**My configuration:**
```kotlin
// AdsConfig
val adsConfig = AdsConfig(
    showInterstitialBeforeNavigate = [true/false],
    // other config...
)

// Usage
[Paste your BaseAds usage code]
```

**Checklist completed:**
- [ ] AAR file added to libs folder
- [ ] Dependencies added to build.gradle.kts
- [ ] AdsLogger.setLogLevel(DEBUG) called
- [ ] Firebase initialized
- [ ] Hilt setup correct
- [ ] Internet connection available
- [ ] Device/emulator setup

**Specific questions:**
1. What do these log messages mean?
2. Why are ads not showing?
3. Are my analytics working?
4. Is my configuration correct?

Please analyze the logs and provide specific debugging steps.
