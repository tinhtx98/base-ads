#!/bin/bash
# BaseAds Debug AAR Builder Script
# Builds debug version with full logging for testing and debugging

set -e  # Exit on any error

echo "🛠️ Building BaseAds DEBUG AAR..."
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Get version from build.gradle.kts
echo -e "${BLUE}📋 Reading version info...${NC}"
VERSION=$(grep -E 'versionName\s*=' base-ads/build.gradle.kts | sed 's/.*versionName = "\(.*\)".*/\1/')
VERSION_CODE=$(grep -E 'versionCode\s*=' base-ads/build.gradle.kts | sed 's/.*versionCode = \(.*\).*/\1/')

if [ -z "$VERSION" ]; then
    VERSION="1.0.0"
    echo -e "${YELLOW}⚠️ Could not detect version, using default: ${VERSION}${NC}"
else
    echo -e "${GREEN}✅ Detected version: ${VERSION} (code: ${VERSION_CODE})${NC}"
fi

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
AAR_NAME="base-ads-debug-v${VERSION}-${TIMESTAMP}.aar"

echo -e "${PURPLE}🐛 DEBUG MODE: Building with full logging enabled${NC}"

echo -e "${BLUE}🧹 Cleaning previous builds...${NC}"
./gradlew :base-ads:clean

echo -e "${BLUE}🔨 Building DEBUG AAR...${NC}"
./gradlew :base-ads:assembleDebug

# Check if build was successful
if [ ! -f "base-ads/build/outputs/aar/base-ads-debug.aar" ]; then
    echo -e "${RED}❌ Build failed! DEBUG AAR file not found.${NC}"
    exit 1
fi

echo -e "${BLUE}📦 Creating export directory...${NC}"
mkdir -p exported-aar-debug

echo -e "${BLUE}📁 Copying DEBUG AAR file...${NC}"
cp base-ads/build/outputs/aar/base-ads-debug.aar "exported-aar-debug/${AAR_NAME}"

# Get file size
FILE_SIZE=$(du -h "exported-aar-debug/${AAR_NAME}" | cut -f1)

echo -e "${GREEN}✅ DEBUG AAR exported successfully!${NC}"
echo "================================================"
echo -e "${GREEN}📦 File: exported-aar-debug/${AAR_NAME}${NC}"
echo -e "${GREEN}📏 Size: ${FILE_SIZE}${NC}"
echo -e "${GREEN}🕒 Built: $(date)${NC}"
echo -e "${PURPLE}🐛 DEBUG: Full logging enabled${NC}"

# Generate DEBUG README
echo -e "${BLUE}📄 Generating DEBUG integration guide...${NC}"
cat > exported-aar-debug/DEBUG-README.md << EOF
# BaseAds Library - DEBUG VERSION

**⚠️ DEBUG BUILD - NOT FOR PRODUCTION ⚠️**

**Version:** ${VERSION}  
**Version Code:** ${VERSION_CODE}  
**Built:** $(date)  
**File:** ${AAR_NAME}  
**Size:** ${FILE_SIZE}  
**Type:** DEBUG with full logging

## 🐛 Debug Features

- ✅ **Full logging enabled** - All AdsLogger.d(), .i(), .w(), .e() messages
- ✅ **Debug symbols included** - Better crash reports
- ✅ **No code obfuscation** - Easier debugging
- ✅ **Source line numbers** - Accurate stack traces
- ✅ **All analytics events logged** - See every interaction

## 📦 What's Included

- ✅ Google Mobile Ads SDK integration
- ✅ ironSource mediation support  
- ✅ Firebase Analytics integration
- ✅ Adaptive banner ads with preloading
- ✅ Interstitial ads with smart timing
- ✅ VIP user management
- ✅ Force update functionality
- ✅ Jetpack Compose UI components
- ✅ Hilt dependency injection ready
- 🐛 **FULL DEBUG LOGGING**

## 🚀 Integration Guide

### 1. Copy DEBUG AAR to your project
\`\`\`
YourProject/
├── app/
│   ├── libs/
│   │   └── ${AAR_NAME}
│   └── build.gradle.kts
\`\`\`

### 2. Add dependencies to app/build.gradle.kts
\`\`\`kotlin
dependencies {
    // BaseAds DEBUG Library
    implementation(files("libs/${AAR_NAME}"))
    
    // Required dependencies
    implementation("com.google.android.gms:play-services-ads:22.5.0")
    implementation("com.ironsource.sdk:mediationsdk:7.5.2")
    implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
    implementation("com.google.firebase:firebase-config-ktx:21.6.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
}
\`\`\`

### 3. Enable logging in your app
\`\`\`kotlin
// In your Application class
class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable debug logging
        AdsLogger.setLogLevel(AdsLogger.LogLevel.DEBUG)
    }
}
\`\`\`

## 🐛 Debug Logging

This DEBUG version will show detailed logs for:

### Banner Ads
\`\`\`
D/BaseAds-Banner: Preloading banner ad...
D/BaseAds-Banner: Banner ad loaded successfully
D/BaseAds-Banner: Showing preloaded banner
\`\`\`

### Interstitial Ads  
\`\`\`
D/BaseAds-Interstitial: Loading interstitial ad...
D/BaseAds-Interstitial: maybeShow called - ready: true
D/BaseAds-Interstitial: Showing interstitial ad
\`\`\`

### Smart Clickable Events
\`\`\`
D/BaseAds-Click: Smart click triggered: save_button
D/BaseAds-Click: Click action completed: save_button
D/BaseAds-Button: Button clicked: Save Settings
D/BaseAds-Button: Post-button-click interstitial attempt - shown: true
\`\`\`

### Search Analytics
\`\`\`
D/BaseAds-Search: Search triggered: query='test query'
D/BaseAds-Keyboard: Search triggered via keyboard: query='test query'
D/BaseAds-Search: Search completed successfully
\`\`\`

### Analytics Events
\`\`\`
D/BaseAds-Analytics: Event logged: button_click
D/BaseAds-Analytics: Event logged: search_performed
D/BaseAds-Analytics: Event logged: click_ad_attempt
\`\`\`

## 🔍 Debugging Tips

1. **Filter Logcat by "BaseAds"** to see all library logs
2. **Check ads loading status** - look for "ready: true/false"
3. **Verify config values** - showInterstitialBeforeNavigate, etc.
4. **Monitor analytics events** - ensure tracking is working
5. **Watch for error messages** - all exceptions are logged

## 📱 Testing Checklist

- [ ] Banner ads show immediately after app start
- [ ] Interstitial ads appear after button clicks  
- [ ] Search analytics are logged properly
- [ ] VIP status affects ad display
- [ ] Error handling works correctly
- [ ] Analytics events are fired

## ⚠️ Important Notes

- **DO NOT use in production** - this has debug overhead
- **Large file size** - includes debug symbols
- **Verbose logging** - may impact performance
- **Use release AAR** for production builds

---

**For production:** Use \`./build-aar.sh\` instead  
**For debugging:** This file is perfect! 🐛
EOF

# Generate DEBUG integration checklist
echo -e "${BLUE}📋 Generating DEBUG integration checklist...${NC}"
cat > exported-aar-debug/DEBUG-INTEGRATION-CHECKLIST.md << EOF
# DEBUG Integration Checklist

## Pre-Integration Setup
- [ ] Remove any existing BaseAds AAR files
- [ ] Clean and rebuild your project
- [ ] Enable debug logging in your app

## File Setup
- [ ] Copy \`${AAR_NAME}\` to \`app/libs/\`
- [ ] Add implementation line to \`build.gradle.kts\`
- [ ] Add all required dependencies
- [ ] Sync project

## Debug Configuration
- [ ] Set \`AdsLogger.setLogLevel(AdsLogger.LogLevel.DEBUG)\`
- [ ] Enable USB debugging on device
- [ ] Connect device and open Logcat
- [ ] Filter Logcat by "BaseAds"

## Test Basic Integration
- [ ] App compiles successfully
- [ ] No import errors for BaseAds classes
- [ ] Hilt integration works (\`@Inject\` annotations)
- [ ] Firebase is initialized

## Test Banner Ads
- [ ] Add banner preloader to your activity
- [ ] Check logs: "Preloading banner ad..."
- [ ] Verify banner appears on screen
- [ ] Check logs: "Banner ad loaded successfully"

## Test Interstitial Ads  
- [ ] Use \`createSmartButtonOnClick()\` on a button
- [ ] Click the button and check logs
- [ ] Should see: "Button clicked: [label]"
- [ ] Should see: "Post-button-click interstitial attempt - shown: true/false"
- [ ] Verify if config.showInterstitialBeforeNavigate is correct

## Test Smart Clickable
- [ ] Use \`Modifier.smartButtonClick()\` on a component
- [ ] Click and check for analytics events
- [ ] Verify ads show (if configured)

## Test Search Functions
- [ ] Use \`createSmartSearchKeyboardActions()\`
- [ ] Perform search and check logs
- [ ] Should see detailed search analytics

## Debug Common Issues

### No Ads Showing
- [ ] Check: \`config.showInterstitialBeforeNavigate = false\`
- [ ] Check: All required parameters passed (ads, config, activity)
- [ ] Check: \`ads.isReady()\` returns true
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
\`\`\`
D/BaseAds-Initializer: Ads initialization completed
D/BaseAds-Banner: Banner ad loaded successfully  
D/BaseAds-Interstitial: Showing interstitial ad
D/BaseAds-Analytics: Event logged: button_click
\`\`\`

### ⚠️ Warning Logs  
\`\`\`
W/BaseAds-Interstitial: Interstitial not ready, skipping
W/BaseAds-Config: showInterstitialBeforeNavigate is true, ads disabled
\`\`\`

### ❌ Error Logs
\`\`\`
E/BaseAds-Ads: Failed to load ad: No internet connection
E/BaseAds-Firebase: Analytics not initialized
E/BaseAds-Hilt: Injection failed for component
\`\`\`

## Final Verification
- [ ] All core features working
- [ ] Logs are clean (no unexpected errors)
- [ ] Analytics events firing correctly  
- [ ] Ready to switch to release AAR

---

**Need help?** Check the logs first - they contain detailed information about what's happening! 🐛
EOF

# Generate debug prompts
echo -e "${BLUE}🤖 Generating DEBUG AI prompts...${NC}"
mkdir -p exported-aar-debug/prompts

cat > exported-aar-debug/prompts/debug-setup-prompt.md << EOF
# BaseAds DEBUG Integration Setup Prompt

Use this prompt with ChatGPT/Claude to get help integrating the BaseAds DEBUG library:

---

**Context:** I'm integrating a DEBUG version of BaseAds library (AAR file) into my Android Jetpack Compose project. This is specifically for debugging and testing purposes.

**Library Details:**
- File: ${AAR_NAME}
- Type: DEBUG AAR with full logging enabled
- Framework: Jetpack Compose + Hilt + Firebase
- Purpose: Test ads integration and debug issues

**My Project Setup:**
- Android SDK: [YOUR_SDK_VERSION]
- Kotlin: [YOUR_KOTLIN_VERSION]  
- Compose BOM: [YOUR_COMPOSE_VERSION]
- Build system: [Gradle/AGP_VERSION]

**What I need help with:**
[Describe your specific issue - e.g., "ads not showing", "compilation errors", "missing logs", etc.]

**Current code:**
\`\`\`kotlin
[Paste your relevant code here]
\`\`\`

**Error logs (if any):**
\`\`\`
[Paste error messages or stack traces]
\`\`\`

**Debug logs from BaseAds:**
\`\`\`
[Paste BaseAds logs from Logcat - filter by "BaseAds"]
\`\`\`

**Questions:**
1. How do I properly integrate this DEBUG AAR?
2. What should I check in the logs to debug my issue?
3. How can I verify the integration is working correctly?

Please provide step-by-step debugging guidance and explain what each log message means.
EOF

cat > exported-aar-debug/prompts/debug-troubleshooting-prompt.md << EOF
# BaseAds DEBUG Troubleshooting Prompt

Use this when you're having issues with the DEBUG version:

---

**Issue:** [Describe your specific problem]

**BaseAds DEBUG Library:**
- File: ${AAR_NAME}
- Version: ${VERSION}
- Type: DEBUG with full logging

**What I'm trying to do:**
[Explain what you're trying to achieve]

**Current behavior:**
[What's actually happening]

**Expected behavior:**
[What should happen]

**Debug logs:**
Filter Logcat by "BaseAds" and paste relevant logs:
\`\`\`
[Paste BaseAds debug logs here]
\`\`\`

**My configuration:**
\`\`\`kotlin
// AdsConfig
val adsConfig = AdsConfig(
    showInterstitialBeforeNavigate = [true/false],
    // other config...
)

// Usage
[Paste your BaseAds usage code]
\`\`\`

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
EOF

echo -e "${BLUE}📊 Generating debug package summary...${NC}"
cat > exported-aar-debug/PACKAGE-CONTENTS.md << EOF
# BaseAds DEBUG Package Contents

## 📦 Files Included

\`\`\`
exported-aar-debug/
├── ${AAR_NAME}                    # Main DEBUG AAR file
├── DEBUG-README.md                # Debug integration guide
├── DEBUG-INTEGRATION-CHECKLIST.md # Step-by-step checklist
├── PACKAGE-CONTENTS.md            # This file
└── prompts/                       # AI assistance prompts
    ├── debug-setup-prompt.md     # Setup help prompt
    └── debug-troubleshooting-prompt.md # Issue resolution prompt
\`\`\`

## 🎯 Quick Start

1. **Copy AAR:** \`${AAR_NAME}\` → \`app/libs/\`
2. **Add dependency:** \`implementation(files("libs/${AAR_NAME}"))\`
3. **Enable logging:** \`AdsLogger.setLogLevel(AdsLogger.LogLevel.DEBUG)\`
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
\`\`\`
D/BaseAds-BannerPreloader: Starting preload...
D/BaseAds-BannerPreloader: Preload completed
D/BaseAds-AdaptiveBanner: Using preloaded ad
\`\`\`

### Interstitial Logs  
\`\`\`
D/BaseAds-Interstitial: maybeShow called
D/BaseAds-Interstitial: Showing interstitial ad
\`\`\`

### Analytics Logs
\`\`\`
D/BaseAds-Analytics: Event logged: button_click
D/BaseAds-Analytics: Event logged: search_performed
\`\`\`

## ⚠️ Remember

- **DEBUG ONLY** - Don't use in production
- **Performance impact** - Debug logging has overhead
- **Large file size** - Includes debug symbols
- **Switch to release** when ready for production

---

**Happy debugging!** 🐛✨
EOF

echo -e "${PURPLE}📦 Creating debug package zip...${NC}"
cd exported-aar-debug
zip -r "../baseads-debug-library.zip" .
cd ..

echo ""
echo -e "${GREEN}🎉 DEBUG AAR Package Ready!${NC}"
echo "================================================"
echo -e "${GREEN}📦 Debug AAR: exported-aar-debug/${AAR_NAME}${NC}"
echo -e "${GREEN}📚 Documentation: exported-aar-debug/DEBUG-README.md${NC}"
echo -e "${GREEN}📋 Checklist: exported-aar-debug/DEBUG-INTEGRATION-CHECKLIST.md${NC}"
echo -e "${GREEN}🤖 AI Prompts: exported-aar-debug/prompts/${NC}"
echo -e "${GREEN}🗜️ Complete Package: baseads-debug-library.zip${NC}"
echo ""
echo -e "${PURPLE}🐛 DEBUG FEATURES ENABLED:${NC}"
echo -e "${PURPLE}   • Full AdsLogger output${NC}"
echo -e "${PURPLE}   • Debug symbols included${NC}"
echo -e "${PURPLE}   • No code obfuscation${NC}"
echo -e "${PURPLE}   • Detailed error reporting${NC}"
echo ""
echo -e "${YELLOW}⚠️  Remember: This is for DEBUGGING only!${NC}"
echo -e "${YELLOW}   Use ./build-aar.sh for production builds${NC}"
echo ""
echo -e "${BLUE}🔍 To start debugging:${NC}"
echo -e "${BLUE}   1. Copy AAR to your project${NC}"
echo -e "${BLUE}   2. Enable debug logging: AdsLogger.setLogLevel(DEBUG)${NC}"
echo -e "${BLUE}   3. Filter Logcat by 'BaseAds'${NC}"
echo -e "${BLUE}   4. Test your integration!${NC}"