# InterstitialAdManager - Setup Guide for Splash Screen Flow

## ❌ Vấn đề Gặp Phải

```kotlin
// ❌ WRONG - isReady() returns false
@Inject
lateinit var interstitialAdManager: InterstitialAdManager

// In SplashActivity after 3s
interstitialAdManager.show(
    activity = activity,
    onShown = { onSplashComplete() }
)
// Result: isReady() == false, ad never shows
```

**Nguyên nhân:** Ad chưa được preload khi `show()` được gọi.

---

## ✅ Giải Pháp

### **Step 1: Preload Ad Sớm (MainActivity)**

Gọi `forcePreload()` **ngay lập tức** khi app launch (trong `onCreate` hoặc `App` class):

```kotlin
// In MainActivity.kt
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var interstitialAdManager: InterstitialAdManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... other init code ...
        
        // START PRELOAD IMMEDIATELY
        interstitialAdManager.forcePreload()
        
        // Then navigate to SplashScreen
        navigateToSplashScreen()
    }
}
```

**Hoặc trong Application class (BEST):**

```kotlin
// In MyApplication.kt
class MyApplication : Application(), DIContainer {
    
    @Inject
    lateinit var interstitialAdManager: InterstitialAdManager
    
    override fun onCreate() {
        super.onCreate()
        // Initialize Hilt, Firebase, etc.
        
        // PRELOAD AD VERY EARLY
        interstitialAdManager.forcePreload()
    }
}
```

---

### **Step 2: Show Ad in SplashScreen (After 3s Delay)**

```kotlin
// In SplashActivity.kt
class SplashActivity : AppCompatActivity() {
    
    @Inject
    lateinit var interstitialAdManager: InterstitialAdManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        lifecycleScope.launch {
            // Wait 3 seconds
            delay(3000)
            
            // At this point, ad should be loaded (if network is good)
            showInterstitialOrNavigate()
        }
    }
    
    private fun showInterstitialOrNavigate() {
        // show() now includes automatic wait logic if ad is still loading
        interstitialAdManager.show(
            activity = this,
            onShown = {
                // Ad was dismissed successfully
                navigateToHomeScreen()
            },
            onNoAdAvailable = {
                // No ad (no fill or error) - proceed to home
                navigateToHomeScreen()
            },
            onShowFailed = { errorMsg ->
                // Ad failed to show - proceed to home
                Log.e("SplashScreen", "Ad failed: $errorMsg")
                navigateToHomeScreen()
            },
            waitTimeoutMs = 2000  // Wait max 2s for ad to load if still loading
        )
    }
    
    private fun navigateToHomeScreen() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
```

---

## 📊 Timeline Comparison

### **❌ BEFORE (No Ad Shows)**
```
0ms    : App launch, MainActivity created
         ↓ (NO PRELOAD CALLED)
0ms    : SplashScreen shown
         ↓
3000ms : show() called, ad NOT loaded yet, isReady() = false
         ↓
         onNoAdAvailable() called immediately
         ↓
3010ms : Navigate to HomeScreen (no ad shown)
```

### **✅ AFTER (Ad Shows)**
```
0ms    : App launch, MainActivity created
         ↓
0ms    : forcePreload() called → AD PRELOAD STARTS
         ↓
0ms    : SplashScreen shown (ad loading in background)
         ↓
1500ms : Ad loaded and cached
         ↓
3000ms : show() called, ad already ready, isReady() = true
         ↓
3000ms : onAdImpression() → Ad shown to user
         ↓
5000ms : User dismisses ad
         ↓
5100ms : onShown() called → Navigate to HomeScreen
```

---

## 🔄 Flow Details

### **What `forcePreload()` does:**
1. Checks if ads are enabled (not VIP, not disabled)
2. Loads interstitial ad in background
3. Registers ILRD listener for revenue tracking
4. Sets status to READY when loaded

### **What `show()` does with new parameters:**
1. **If ad is ready:** Shows immediately ✅
2. **If ad is still loading:** Waits up to `waitTimeoutMs` (default 2s) ⏳
3. **If ad never loads:** Calls `onNoAdAvailable()` callback 
4. **If show fails:** Calls `onShowFailed()` callback with error

---

## 🎯 Key Points

✅ **DO:**
- Call `forcePreload()` as early as possible (App.onCreate or MainActivity.onCreate)
- Wait at least 2-3 seconds on SplashScreen to give ad time to load
- Use all 3 callbacks: `onShown`, `onNoAdAvailable`, `onShowFailed`
- Check `AdsLogger` output to debug ad loading

❌ **DON'T:**
- Call `show()` immediately without preloading first
- Assume ad is ready without calling `forcePreload()` earlier
- Ignore `onNoAdAvailable` callback - always have fallback
- Use too short splash screen delay (minimum 2-3 seconds recommended)

---

## 📝 Complete Example

```kotlin
// MyApplication.kt
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize dependencies first
        initializeHilt()
        initializeFirebase()
        
        // Then preload ads ASAP
        val interstitialAdManager = getInterstitialAdManager()
        interstitialAdManager.forcePreload()
    }
}

// SplashActivity.kt
class SplashActivity : AppCompatActivity() {
    @Inject lateinit var interstitialAdManager: InterstitialAdManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            delay(3000) // Enough time for ad to load
            attemptToShowInterstitial()
        }
    }
    
    private fun attemptToShowInterstitial() {
        interstitialAdManager.show(
            activity = this,
            onShown = { goHome() },
            onNoAdAvailable = { goHome() },
            onShowFailed = { goHome() },
            waitTimeoutMs = 2000
        )
    }
    
    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
```

---

## 🐛 Debugging

Check AdsLogger output:
```
D/Interstitial: Force preloading interstitial ad
D/Interstitial: Starting interstitial preload
...
D/Interstitial: Interstitial ad loaded successfully
D/Interstitial: Force showing interstitial ad (bypassing policy)
D/Interstitial: Interstitial ad impression
```

If you don't see these logs, ad preload might not be called early enough.
