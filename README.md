# Base Ads Module

A comprehensive Android library for managing AdMob and ironSource mediation ads with smart show policies, VIP gate functionality, and Firebase Analytics integration.

## Features

- 🎯 **Smart Ad Display**: Policy-safe interstitial ads with cooldown, daily caps, and route blocking
- 📱 **Adaptive Banner Ads**: Anchored adaptive banners that automatically calculate optimal size
- 👑 **VIP Gate**: Disable ads for premium users
- 📊 **Analytics Integration**: Firebase Analytics with detailed event tracking
- 🛡️ **Policy Compliance**: Built-in safety measures to prevent policy violations
- 🧪 **Debug Tools**: Debug-only logging with request/impression tracking
- 🔧 **Extensible**: Easy to integrate and customize for any app

## Setup

### 1. Repository Configuration

Add the ironSource repository to your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://android-sdk.is.com/") // ironSource repository
    }
}
```

### 2. Dependencies

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    // Base Ads Module
    implementation(project(":base-ads"))
    
    // Firebase (for analytics)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Hilt (for dependency injection)
    implementation("com.google.dagger:hilt-android:2.56.1")
    kapt("com.google.dagger:hilt-compiler:2.56.1")
    
    // Other dependencies...
}
```

### 3. AndroidManifest.xml

Add required permissions and AdMob Application ID to your app's `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Required permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="com.google.android.gms.permission.AD_ID" />
    
    <application
        android:name=".YourApplication"
        ... >
        
        <!-- AdMob Application ID -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="@string/admob_app_id"/>
        
        <!-- Your activities... -->
    </application>
</manifest>
```

Add your AdMob Application ID to `strings.xml`:

```xml
<resources>
    <string name="admob_app_id">ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX</string>
</resources>
```

### 4. Application Class

Initialize the ads module in your Application class:

```kotlin
@HiltAndroidApp
class YourApplication : Application() {
    
    @Inject
    lateinit var adsInitializer: AdsInitializer
    
    @Inject 
    lateinit var adsPrefs: AdsPrefs
    
    override fun onCreate() {
        super.onCreate()
        
        // Track app launches
        adsPrefs.incrementAppLaunchCount()
        
        // Initialize ads
        adsInitializer.initializeWithTestDevices(
            context = this,
            includeCommonTestDevices = BuildConfig.DEBUG
        )
    }
}
```

## Usage

### 1. Banner Ads

Add banner ads to your Compose UI:

```kotlin
@Composable
fun YourScreen(
    adUnitsProvider: AdUnitsProvider,
    adsConfig: AdsConfig,
    vipGate: VipGate,
    analyticsLogger: AnalyticsLogger
) {
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
        // Your content
    }
}
```

### 2. Interstitial Ads

#### Smart Navigation with Ads

```kotlin
@Composable
fun YourScreen(
    navController: NavController,
    interstitialAdManager: InterstitialAdManager,
    analyticsLogger: AnalyticsLogger,
    adsConfig: AdsConfig,
    activity: Activity
) {
    val coroutineScope = rememberCoroutineScope()
    
    Button(
        onClick = {
            coroutineScope.launch {
                navController.navigateSmartSimple(
                    route = "destination",
                    ads = interstitialAdManager,
                    analytics = analyticsLogger,
                    config = adsConfig,
                    activity = activity
                )
            }
        }
    ) {
        Text("Navigate with Smart Ads")
    }
}
```

#### Smart Clickable with Ads

```kotlin
@Composable
fun YourButton(
    interstitialAdManager: InterstitialAdManager,
    analyticsLogger: AnalyticsLogger,
    adsConfig: AdsConfig,
    activity: Activity
) {
    Text(
        text = "Click me!",
        modifier = Modifier.smartClickableSimple(
            label = "my_button",
            ads = interstitialAdManager,
            analytics = analyticsLogger,
            config = adsConfig,
            activity = activity
        ) {
            // Your click action
        }
    )
}
```

#### Manual Interstitial Display

```kotlin
class YourViewModel @Inject constructor(
    private val interstitialAdManager: InterstitialAdManager
) : ViewModel() {
    
    suspend fun showInterstitialIfReady(activity: Activity) {
        val shown = interstitialAdManager.maybeShow(
            activity = activity,
            currentRoute = "your_screen"
        )
        
        if (shown) {
            // Ad was shown
        } else {
            // Ad was not shown (policy restrictions, no ad ready, etc.)
        }
    }
}
```

### 3. Screen Tracking

Mark screens as opened for proper ad timing:

```kotlin
@Composable
fun YourScreen(
    interstitialAdManager: InterstitialAdManager,
    analyticsLogger: AnalyticsLogger
) {
    // This should be called in every screen
    MarkScreenOpened(
        ads = interstitialAdManager,
        analytics = analyticsLogger,
        screenName = "your_screen"
    )
    
    // Your screen content
}
```

## Configuration

### 1. Custom Ads Configuration

Create a custom Hilt module to override default settings:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object YourAdsModule {
    
    @Provides
    @Singleton
    fun provideAdsConfig(): AdsConfig {
        return AdsConfig(
            enableAds = true,
            enableInterstitial = true,
            enableBanner = true,
            interstitialBlocklistRoutes = setOf(
                "premium",
                "vip",
                "subscription", 
                "auth",
                "login",
                "register",
                "checkout",
                "payment",
                "purchase",
                "billing",
                "settings" // Add your app-specific routes
            ),
            showInterstitialBeforeNavigate = false // or true based on your preference
        )
    }
}
```

### 2. Production Ad Units

Replace test ad units with your production ones:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ProductionAdsModule {
    
    @Provides
    @Singleton
    fun provideAdUnitsProvider(): AdUnitsProvider {
        return ProductionAdUnitsProvider(
            bannerUnitId = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX",
            interstitialUnitId = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
        )
    }
}
```

### 3. VIP Gate Implementation

Implement VIP functionality to disable ads for premium users:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object VipModule {
    
    @Provides
    @Singleton
    fun provideVipGate(
        userRepository: UserRepository // Your user repository
    ): VipGate {
        return object : VipGate {
            override fun isVip(): Boolean {
                return userRepository.isPremiumUser()
            }
        }
    }
}
```

## AdMob Mediation Setup

### 1. AdMob Console Configuration

1. Login to [AdMob Console](https://admob.google.com)
2. Go to **Mediation** → **Mediation Groups**
3. Create a new mediation group for your ad units
4. Add **ironSource** as a mediation adapter
5. Configure ironSource settings:
   - **App Key**: Your ironSource app key
   - **Instance ID**: Create instances for different placements

### 2. ironSource Configuration

1. Login to [ironSource Console](https://platform.ironsrc.com)
2. Create your app and ad units
3. Configure mediation settings to work with AdMob
4. Note down your App Key and Instance IDs

### 3. app-ads.txt

Add the following to your website's `app-ads.txt` file:

```
google.com, pub-XXXXXXXXXXXXXXXX, DIRECT, f08c47fec0942fa0
ironsrc.com, XXXXX, DIRECT
```

Replace with your actual publisher IDs.

## Policy Guidelines

### Safe Ad Display Practices

The Base Ads module includes built-in safety measures:

1. **Cooldown Period**: 45 seconds between interstitial ads
2. **Daily Cap**: Maximum 12 interstitials per day
3. **First Launch Delay**: 20 seconds after app launch
4. **Screen Open Delay**: 5 seconds after entering a screen
5. **Route Blocking**: No ads on sensitive screens (payment, auth, etc.)

### Recommended Display Points

Show interstitial ads at natural transition points:

- ✅ After completing a level/task
- ✅ Between different sections
- ✅ After user-initiated actions
- ❌ Immediately on app open
- ❌ During user input
- ❌ On payment/auth screens

## Testing

### Test Ad Units

The module includes Google's test ad unit IDs by default:

- **Banner**: `ca-app-pub-3940256099942544/6300978111`
- **Interstitial**: `ca-app-pub-3940256099942544/1033173712`

### Test Device Configuration

Add your device ID for testing:

```kotlin
adsInitializer.initialize(
    context = this,
    testDeviceIds = listOf(
        "YOUR_DEVICE_ID_HERE",
        RequestConfiguration.TEST_DEVICE_ID_EMULATOR
    )
)
```

### Debug Logging

Enable debug logging in development builds:

```kotlin
// In build.gradle.kts
buildTypes {
    debug {
        buildConfigField("boolean", "DEBUG_LOG_ENABLED", "true")
    }
    release {
        buildConfigField("boolean", "DEBUG_LOG_ENABLED", "false")
    }
}
```

## Analytics Events

The module automatically logs these Firebase Analytics events:

### Ads Events
- `ads_init`: When SDK initializes
- `ad_banner_loaded`: Banner ad loads successfully
- `ad_banner_load_failed`: Banner ad fails to load
- `ad_banner_impression`: Banner ad impression
- `ad_banner_clicked`: Banner ad clicked
- `ad_interstitial_loaded`: Interstitial ad loads
- `ad_interstitial_load_failed`: Interstitial ad fails to load
- `ad_interstitial_impression`: Interstitial ad impression
- `ad_interstitial_dismissed`: Interstitial ad dismissed
- `ad_interstitial_show_failed`: Interstitial ad fails to show

### User Interaction Events
- `click`: Generic click events with label
- `nav_click`: Navigation clicks with route
- `nav_success`: Successful navigation
- `nav_error`: Navigation errors
- `screen_view`: Screen views (automatic)

## Troubleshooting

### Common Issues

1. **Ads not showing**
   - Check VIP status (`vipGate.isVip()`)
   - Verify ads are enabled in config
   - Check policy restrictions (cooldown, daily cap, etc.)
   - Ensure ad units are correctly configured

2. **Banner not displaying**
   - Check internet connection
   - Verify ad unit ID is correct
   - Check if VIP status is enabled

3. **Build errors**
   - Ensure all dependencies are added
   - Check Hilt setup is correct
   - Verify KSP is configured properly

### Debug Information

Get debug information about ads state:

```kotlin
val debugInfo = interstitialAdManager.getDebugInfo()
val prefsInfo = adsPrefs.getDebugInfo()
```

## Performance Considerations

1. **Preload ads proactively**: Call `interstitialAdManager.preload()` early
2. **Memory management**: Ads are automatically cleaned up on Activity destroy
3. **Thread safety**: All operations are main-thread safe
4. **Network efficiency**: Ads are cached and reused when possible

## Version Compatibility

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Kotlin**: 2.1.20+
- **Compose**: Latest stable
- **AdMob SDK**: 23.5.0+
- **ironSource**: 8.4.0+

## License

This module is provided as-is for educational and development purposes. Ensure compliance with Google AdMob and ironSource policies when using in production.

## Support

For issues and feature requests, please check the implementation details in the source code or contact the development team.