# 🎯 BaseAds AAR Integration - Prompt Usage Guide

## 📦 **PACKAGE CONTENTS**

Sau khi extract `baseads-library-with-prompts.zip`, bạn sẽ có:

```
exported-aar/
├── base-ads-v1.0.0-TIMESTAMP.aar           # AAR library file
├── README.md                               # Complete integration guide
├── INTEGRATION_CHECKLIST.md               # Step-by-step checklist
├── proguard-rules.pro                     # ProGuard rules
├── INTEGRATION_PROMPT_TEMPLATE.md         # Advanced prompt templates
└── QUICK_INTEGRATION_PROMPTS.md           # Quick copy-paste prompts
```

## 🚀 **HOW TO USE PROMPTS**

### **Step 1: Choose Your Approach**

#### **🔰 For Beginners:**
Use `QUICK_INTEGRATION_PROMPTS.md` → Copy **"Basic Integration"** prompt

#### **⚡ For Quick Setup:**
Use this one-liner prompt:
```
Integrate BaseAds AAR into Android Jetpack Compose project:
AAR: base-ads-v1.0.0.aar
Project: Compose + Hilt + Firebase
Features: Banner ads at bottom + Interstitial ads
Goal: Production-ready implementation
Provide complete setup with all necessary files and configuration.
```

#### **🏗️ For Advanced Users:**
Use `INTEGRATION_PROMPT_TEMPLATE.md` → Customize the detailed template

### **Step 2: Customize Your Prompt**

1. **Replace project details:**
   - `[YOUR_PROJECT_NAME]` → Your actual project name
   - `[MVVM/MVI/Clean]` → Your architecture pattern
   - `[Compose/Views]` → Your UI framework

2. **Specify requirements:**
   - Banner ad placement
   - Interstitial timing
   - VIP user logic
   - Special constraints

3. **Add current setup:**
   - Existing dependencies
   - Current architecture
   - Any conflicts to avoid

### **Step 3: Send to AI Assistant**

Copy your customized prompt and send to:
- **ChatGPT** (GPT-4 recommended)
- **Claude** (Anthropic)
- **GitHub Copilot Chat**
- **Any code-capable AI**

## 📋 **QUICK REFERENCE**

### **Most Common Prompt (Copy This):**
```
Integrate BaseAds AAR into Android Jetpack Compose project with Hilt:

Project: Android app with Compose + Hilt DI
AAR: base-ads-v1.0.0.aar (136K)
Need: Banner ads + Interstitial ads + Firebase integration

Provide:
1. Complete file setup (dependencies, Firebase)
2. Application class with @HiltAndroidApp
3. AdsModule configuration
4. MainActivity with banner integration
5. AndroidManifest setup
6. ProGuard rules
7. Testing instructions

Make it production-ready with test ad units first.
```

### **For Troubleshooting:**
```
BaseAds AAR integration issues - need debugging help:

Problem: [Describe your specific issue]
Error logs: [Paste relevant logcat output]
Current setup: [Describe what you've implemented]

Fix the integration and provide working solution.
```

## 🎯 **SUCCESS CHECKLIST**

After using prompts and implementing the code:

- ✅ App builds without errors
- ✅ Banner ads display at bottom
- ✅ Interstitial ads can be triggered
- ✅ Logs show "BaseAds-Initializer" messages
- ✅ VIP functionality works
- ✅ Firebase integration complete

## 💡 **PRO TIPS**

### **1. Be Specific**
Instead of: "Add ads to my app"
Use: "Add banner ads to main screen bottom and interstitial ads between screens"

### **2. Include Context**
Mention your current setup:
- Existing dependencies
- Architecture patterns
- UI framework used
- Any special requirements

### **3. Test Incrementally**
1. Start with basic banner integration
2. Add interstitial ads
3. Implement VIP logic
4. Add force update feature

### **4. Use Test Ad Units First**
Always test with provided test ad unit IDs before switching to production.

## 🚨 **COMMON SCENARIOS**

### **Scenario 1: New Project**
Use **"Basic Integration"** prompt from `QUICK_INTEGRATION_PROMPTS.md`

### **Scenario 2: Existing App**
Use **"Advanced Integration"** and mention your current architecture

### **Scenario 3: Having Issues**
Use **"Troubleshooting"** prompt with specific error details

### **Scenario 4: Custom Requirements**
Use **"Custom Requirements"** prompt and specify your needs

## 📚 **LEARNING PATH**

1. **Start Here:** `README.md` - Understand what BaseAds provides
2. **Quick Setup:** `QUICK_INTEGRATION_PROMPTS.md` - Copy basic prompt
3. **Follow Steps:** `INTEGRATION_CHECKLIST.md` - Verify each step
4. **Advanced Usage:** `INTEGRATION_PROMPT_TEMPLATE.md` - Custom scenarios
5. **Production:** Apply ProGuard rules and production ad units

## 🎉 **EXPECTED RESULTS**

Using these prompts, you should get:

- ✅ **Complete working code** ready to copy-paste
- ✅ **Step-by-step instructions** for implementation
- ✅ **All necessary files** (Application class, modules, etc.)
- ✅ **Configuration examples** for different scenarios
- ✅ **Testing guidance** and troubleshooting tips
- ✅ **Production deployment** instructions

## 🔗 **NEXT STEPS**

1. Extract this package
2. Choose appropriate prompt from `QUICK_INTEGRATION_PROMPTS.md`
3. Customize with your project details
4. Send to AI assistant
5. Follow the provided implementation
6. Test and verify integration
7. Deploy to production

---

**🚀 Ready to integrate BaseAds? Start with the Basic Integration prompt!**