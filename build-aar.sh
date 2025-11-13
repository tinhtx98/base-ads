#!/bin/bash
# BaseAds AAR Builder Script
# Automatically builds and exports base-ads module as AAR file

set -e  # Exit on any error

echo "🏗️ Building BaseAds AAR..."
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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
AAR_NAME="base-ads.aar"

echo -e "${BLUE}🧹 Cleaning previous builds...${NC}"
./gradlew :base-ads:clean

echo -e "${BLUE}🔨 Building release AAR...${NC}"
./gradlew :base-ads:assembleRelease

# Check if build was successful
if [ ! -f "base-ads/build/outputs/aar/base-ads-release.aar" ]; then
    echo -e "${RED}❌ Build failed! AAR file not found.${NC}"
    exit 1
fi

echo -e "${BLUE}📦 Creating export directory...${NC}"
mkdir -p exported-aar

echo -e "${BLUE}📁 Copying AAR file...${NC}"
cp base-ads/build/outputs/aar/base-ads-release.aar "exported-aar/${AAR_NAME}"

# Get file size
FILE_SIZE=$(du -h "exported-aar/${AAR_NAME}" | cut -f1)

echo -e "${GREEN}✅ AAR exported successfully!${NC}"
echo "================================================"
echo -e "${GREEN}📦 File: exported-aar/${AAR_NAME}${NC}"
echo -e "${GREEN}📏 Size: ${FILE_SIZE}${NC}"
echo -e "${GREEN}🕒 Built: $(date)${NC}"

# Generate README for AAR
echo -e "${BLUE}📄 Generating integration guide...${NC}"
cat > exported-aar/README.md << EOF
# BaseAds Library

**Version:** ${VERSION}  
**Version Code:** ${VERSION_CODE}  
**Built:** $(date)  
**File:** ${AAR_NAME}  
**Size:** ${FILE_SIZE}

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

## 🚀 Integration Guide

### 1. Copy AAR to your project
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
    // BaseAds Library
    implementation(files("libs/${AAR_NAME}"))
    
    // Required dependencies
    implementation("com.google.android.gms:play-services-ads:22.5.0")
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    implementation("com.google.firebase:firebase-config:21.4.1")
    implementation("com.ironsource.sdk:mediationsdk:7.5.1")
    
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.4")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
\`\`\`

### 3. Enable Jetpack Compose in android block
\`\`\`kotlin
android {
    compileSdk 34
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}
\`\`\`

### 4. Setup Application class
\`\`\`kotlin
import com.tinhtx.baseads.core.AdsInitializer
import com.tinhtx.baseads.data.AdsPrefs
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject
    lateinit var adsInitializer: AdsInitializer
    
    @Inject 
    lateinit var adsPrefs: AdsPrefs
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ads
        adsPrefs.incrementAppLaunchCount()
        adsInitializer.initializeWithTestDevices(
            context = this,
            includeCommonTestDevices = true
        )
    }
}
\`\`\`

### 5. Add to AndroidManifest.xml
\`\`\`xml
<application
    android:name=".MyApplication"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/Theme.YourApp">
    
    <!-- Google AdMob App ID -->
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-3940256099942544~3347511713" />
    
    <!-- Your activities here -->
    
</application>
\`\`\`

### 6. Configure ads (Create AdsModule.kt)
\`\`\`kotlin
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AdUnitsProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MyAppAdsModule {
    
    @Provides
    @Singleton
    fun provideAdsConfig(): AdsConfig = AdsConfig(
        enableAds = true,
        enableInterstitial = true,
        enableBanner = true,
        ironSourceAppKey = "YOUR_IRONSOURCE_APP_KEY", // Replace with your key
        enableIronSourceLogging = BuildConfig.DEBUG
    )
    
    @Provides
    @Singleton 
    fun provideAdUnitsProvider(): AdUnitsProvider = object : AdUnitsProvider {
        override val bannerAdUnitId: String = "ca-app-pub-3940256099942544/6300978111" // Test ID
        override val interstitialAdUnitId: String = "ca-app-pub-3940256099942544/1033173712" // Test ID
        override val rewardedAdUnitId: String = "ca-app-pub-3940256099942544/5224354917" // Test ID
    }
}
\`\`\`

### 7. Use banner ads in Compose
\`\`\`kotlin
import com.tinhtx.baseads.banner.AdaptiveBanner
import com.tinhtx.baseads.core.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainScreen() {
    val adUnitsProvider: AdUnitsProvider = hiltViewModel()
    val adsConfig: AdsConfig = hiltViewModel()
    val vipGate: VipGate = hiltViewModel()
    val analyticsLogger: AnalyticsLogger = hiltViewModel()
    
    Scaffold(
        bottomBar = {
            AdaptiveBanner(
                adUnitsProvider = adUnitsProvider,
                adsConfig = adsConfig,
                vipGate = vipGate,
                analyticsLogger = analyticsLogger
            )
        }
    ) { paddingValues ->
        // Your content here
    }
}
\`\`\`

## 🔧 Configuration

### Ad Unit IDs
Replace test ad unit IDs with your production IDs:
- Banner: \`ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX\`
- Interstitial: \`ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX\`

### ironSource Integration
1. Get your app key from ironSource dashboard
2. Replace \`YOUR_IRONSOURCE_APP_KEY\` in AdsModule
3. Add ironSource adapters as needed

### Firebase Setup
1. Add \`google-services.json\` to app folder
2. Add Firebase plugin to app/build.gradle.kts:
   \`\`\`kotlin
   plugins {
       id("com.google.gms.google-services")
   }
   \`\`\`

## 📚 API Reference

### Key Classes
- \`AdsInitializer\` - Initialize ads SDK
- \`AdaptiveBanner\` - Compose banner component  
- \`InterstitialAdManager\` - Manage interstitial ads
- \`VipGate\` - VIP user management
- \`ForceUpdateDialog\` - App update enforcement

### Features
- ✅ Adaptive banners with preloading
- ✅ Smart interstitial timing
- ✅ VIP user ad-free experience
- ✅ Force update with Firebase Remote Config
- ✅ Analytics integration
- ✅ ironSource mediation support

## 🆘 Support

For issues and questions:
- Check logs with tag: \`BaseAds-*\`
- Enable debug logging in AdsConfig
- Test with provided test ad unit IDs first

---
**Generated:** $(date)  
**BaseAds Library v${VERSION}**
EOF

# Generate ProGuard rules
echo -e "${BLUE}🛡️ Generating ProGuard rules...${NC}"
cat > exported-aar/proguard-rules.pro << 'EOF'
# BaseAds Library ProGuard Rules

# Google Mobile Ads
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ironSource
-keepclassmembers class com.ironsource.** { public *; }
-keep class com.ironsource.** { *; }
-dontwarn com.ironsource.**

# Meta Audience Network
-keep class com.facebook.ads.** { *; }
-keepclassmembers class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**
-dontwarn com.facebook.infer.annotation.**
-keep class com.facebook.infer.annotation.** { *; }

# InMobi
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
-keep class com.inmobi.ads.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# BaseAds
-keep class com.tinhtx.baseads.** { *; }
-dontwarn com.tinhtx.baseads.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
EOF

# Copy prompt templates
echo -e "${BLUE}📝 Adding integration prompt templates...${NC}"
cp INTEGRATION_PROMPT_TEMPLATE.md exported-aar/
cp QUICK_INTEGRATION_PROMPTS.md exported-aar/

# Generate integration checklist
echo -e "${BLUE}📋 Generating integration checklist...${NC}"
cat > exported-aar/INTEGRATION_CHECKLIST.md << EOF
# BaseAds Integration Checklist

## ✅ Pre-Integration
- [ ] Android Studio project with Kotlin support
- [ ] Min SDK 21+ (recommended 26+)
- [ ] Jetpack Compose enabled
- [ ] Hilt dependency injection setup
- [ ] Firebase project created

## ✅ File Setup
- [ ] Copy \`${AAR_NAME}\` to \`app/libs/\`
- [ ] Add dependencies to \`build.gradle.kts\`
- [ ] Copy \`proguard-rules.pro\` content to your ProGuard config
- [ ] Add \`google-services.json\` to \`app/\`

## ✅ Code Integration  
- [ ] Create Application class with \`@HiltAndroidApp\`
- [ ] Inject and initialize \`AdsInitializer\`
- [ ] Create \`AdsModule\` with your ad unit IDs
- [ ] Add AdMob application ID to \`AndroidManifest.xml\`
- [ ] Replace test ad unit IDs with production IDs

## ✅ Testing
- [ ] Build and run app successfully
- [ ] Check logs for "BaseAds-Initializer" messages
- [ ] Test banner ads display
- [ ] Test interstitial ads
- [ ] Verify VIP functionality
- [ ] Test force update dialog

## ✅ Production Ready
- [ ] Replace test device IDs
- [ ] Set production ad unit IDs
- [ ] Configure ironSource app key
- [ ] Setup Firebase Remote Config for force update
- [ ] Test on physical devices
- [ ] Enable ProGuard/R8 optimization

## 🚨 Common Issues
- **Banner not showing:** Check ad unit ID and internet connection
- **Hilt injection fails:** Ensure \`@HiltAndroidApp\` is added
- **Compose errors:** Verify Compose BOM version compatibility
- **ironSource issues:** Check app key and adapter configuration

---
Last updated: $(date)
EOF

echo ""
echo "================================================"
echo -e "${GREEN}🎉 BaseAds AAR Package Complete!${NC}"
echo "================================================"
echo -e "${YELLOW}📁 Files created:${NC}"
echo "   • exported-aar/${AAR_NAME}"
echo "   • exported-aar/README.md"
echo "   • exported-aar/proguard-rules.pro"
echo "   • exported-aar/INTEGRATION_CHECKLIST.md"
echo "   • exported-aar/INTEGRATION_PROMPT_TEMPLATE.md"
echo "   • exported-aar/QUICK_INTEGRATION_PROMPTS.md"
echo ""
echo -e "${BLUE}📤 Ready to distribute:${NC}"
echo "   zip -r baseads-library.zip exported-aar/"
echo ""
echo -e "${GREEN}✨ Happy coding! ✨${NC}"