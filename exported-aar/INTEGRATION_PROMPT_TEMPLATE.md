# BaseAds AAR Integration Prompt Template

## 🎯 **PROMPT CHÍNH:**

```
Tôi có file BaseAds AAR library và cần tích hợp vào Android project hiện tại. Hãy giúp tôi thực hiện đầy đủ các bước sau:

**Context:**
- AAR file: base-ads-v1.0.0-TIMESTAMP.aar  
- Project: Android app với Kotlin
- Framework: Jetpack Compose + Hilt
- Target: Tích hợp ads (banner, interstitial) với Firebase Analytics

**Yêu cầu thực hiện:**

1. **SETUP FILES:**
   - Copy AAR vào app/libs/
   - Cấu hình dependencies trong build.gradle.kts
   - Setup Firebase (google-services.json, plugins)

2. **CODE INTEGRATION:**
   - Tạo Application class với @HiltAndroidApp
   - Tạo AdsModule cho dependency injection
   - Cấu hình ad unit IDs (test + production)
   - Setup AndroidManifest.xml với permissions và meta-data

3. **UI IMPLEMENTATION:**
   - Integrate AdaptiveBanner vào Compose UI
   - Setup InterstitialAdManager
   - Implement ForceUpdateDialog
   - Handle VIP user logic

4. **CONFIGURATION:**
   - ProGuard rules
   - ironSource integration (nếu cần)
   - Firebase Remote Config for force update
   - Logging và debugging setup

5. **TESTING & VERIFICATION:**
   - Build và run app
   - Verify ads display correctly
   - Check logs for initialization
   - Test VIP functionality

**Output mong muốn:**
- Code files hoàn chỉnh và sẵn sàng chạy
- Clear step-by-step instructions
- Troubleshooting tips cho common issues
- Production-ready configuration examples

Hãy thực hiện từng bước một cách chi tiết và cung cấp code examples cụ thể.
```

## 🔧 **PROMPT VARIATIONS:**

### **Cho Beginner:**
```
Tôi là người mới với Android development và cần tích hợp BaseAds AAR library vào project. Hãy giải thích từng bước một cách đơn giản và chi tiết, bao gồm:

- Tại sao cần thêm dependency này
- File nào cần tạo/sửa và vì sao  
- Code example với comments giải thích
- Common mistakes cần tránh
- Cách debug khi có vấn đề

AAR file: base-ads-v1.0.0.aar
Project setup: [mô tả project hiện tại]
```

### **Cho Advanced Developer:**
```
Integrate BaseAds AAR into existing production Android app with:
- Multi-module architecture
- Existing Hilt setup
- Custom Application class
- ProGuard/R8 enabled
- CI/CD pipeline

Requirements:
- Minimal code changes
- Maintain existing architecture
- Production-ready configuration
- Performance optimized
- Proper error handling

AAR: base-ads-v1.0.0.aar
Current setup: [describe existing architecture]
```

### **Cho Specific Use Case:**
```
Tôi cần integrate BaseAds AAR với yêu cầu đặc biệt:

**Use case:** [Banner ads chỉ ở màn hình chính / Interstitial ads giữa các flow / etc.]
**Constraints:** [VIP users không thấy ads / Ads chỉ sau 3 lần mở app / etc.]
**Architecture:** [MVVM / MVI / Clean Architecture / etc.]

Hãy customize integration phù hợp với requirements này.

AAR file: base-ads-v1.0.0.aar
```

## 📋 **TEMPLATE ĐẦY ĐỦ CHO COPY-PASTE:**

```
# BaseAds AAR Integration Request

## 📱 Project Context
- **Project Type:** [Android App / Library / Multi-module]
- **Architecture:** [MVVM / MVI / Clean Architecture]
- **UI Framework:** [Jetpack Compose / View System / Hybrid]
- **DI Framework:** [Hilt / Dagger / Koin / Manual]
- **Current SDK Versions:** [Min/Target/Compile SDK]
- **Existing Dependencies:** [List key dependencies]

## 📦 AAR Details
- **File:** base-ads-v1.0.0-TIMESTAMP.aar
- **Size:** 136K
- **Features Needed:** [Banner / Interstitial / VIP / Force Update / All]

## 🎯 Integration Requirements

### 1. **File Setup**
- [ ] Copy AAR to correct location
- [ ] Configure build.gradle.kts dependencies
- [ ] Setup Firebase integration
- [ ] Configure ProGuard rules

### 2. **Code Integration** 
- [ ] Application class setup
- [ ] Hilt modules configuration
- [ ] Ad unit IDs setup (test + production)
- [ ] AndroidManifest configuration

### 3. **UI Implementation**
- [ ] Banner ads integration
- [ ] Interstitial ads management
- [ ] Force update dialog
- [ ] VIP user handling

### 4. **Configuration**
- [ ] ironSource setup (app key: [YOUR_KEY])
- [ ] Firebase Remote Config
- [ ] Logging configuration
- [ ] Testing setup

## 🚨 Special Requirements
- [Any specific constraints or customizations needed]

## 📝 Expected Output
Provide complete, working code with:
- Step-by-step implementation guide
- Code files ready to copy-paste  
- Testing instructions
- Troubleshooting tips
- Production deployment notes

---
**Start integration now:**
```

## 🚀 **QUICK START PROMPTS:**

### **Prompt 1: Cơ bản**
```
Tích hợp BaseAds AAR vào Android project với Jetpack Compose và Hilt. Cần banner ads ở bottom và interstitial ads. Provide complete setup code.

AAR: base-ads-v1.0.0.aar
```

### **Prompt 2: Với context**
```
Tôi có Android app sử dụng Compose + Hilt. Cần add BaseAds AAR để show banner ads ở main screen và interstitial ads khi navigate. Hãy tạo all necessary files và configuration.

Current project structure:
- MainActivity với Compose
- Hilt đã setup
- Firebase chưa có

AAR file: base-ads-v1.0.0.aar
```

### **Prompt 3: Troubleshooting**
```
Đã integrate BaseAds AAR nhưng gặp issues:
- [Describe specific issues]
- Logs: [Paste relevant logs]

Hãy debug và fix problems. 

Setup hiện tại:
- [Describe current setup]
```

## 💡 **USAGE TIPS:**

1. **Customize prompt** theo project cụ thể
2. **Provide context** về architecture hiện tại  
3. **Specify requirements** rõ ràng
4. **Include error logs** nếu troubleshooting
5. **Mention constraints** (VIP users, specific screens, etc.)

## 📖 **EXAMPLE USAGE:**

```
Tôi có Android app bán hàng online cần tích hợp BaseAds AAR. Requirements:

- Banner ads chỉ ở home screen và product list
- Interstitial ads sau khi add to cart (không quá 1 lần/phút)
- VIP customers (premium subscription) không thấy ads
- Force update khi có version mới critical

Project sử dụng:
- Jetpack Compose + Navigation
- Hilt dependency injection  
- Room database
- Retrofit API
- Firebase Analytics đã có

AAR: base-ads-v1.0.0-20251002_142159.aar

Hãy implement từng bước với complete code examples.
```

---

**🎉 Copy một trong các prompts trên và customize theo project của bạn để get comprehensive integration support!**