# BaseAds Integration Checklist

## ✅ Pre-Integration
- [ ] Android Studio project with Kotlin support
- [ ] Min SDK 21+ (recommended 26+)
- [ ] Jetpack Compose enabled
- [ ] Hilt dependency injection setup
- [ ] Firebase project created

## ✅ File Setup
- [ ] Copy `base-ads-v1.0.0-20251015_222918.aar` to `app/libs/`
- [ ] Add dependencies to `build.gradle.kts`
- [ ] Copy `proguard-rules.pro` content to your ProGuard config
- [ ] Add `google-services.json` to `app/`

## ✅ Code Integration  
- [ ] Create Application class with `@HiltAndroidApp`
- [ ] Inject and initialize `AdsInitializer`
- [ ] Create `AdsModule` with your ad unit IDs
- [ ] Add AdMob application ID to `AndroidManifest.xml`
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
- **Hilt injection fails:** Ensure `@HiltAndroidApp` is added
- **Compose errors:** Verify Compose BOM version compatibility
- **ironSource issues:** Check app key and adapter configuration

---
Last updated: Wed Oct 15 22:29:26 +07 2025
