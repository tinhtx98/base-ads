/**
 * Example usage of InterstitialAdManager.show() for SplashScreen flow
 * 
 * Flow:
 * 1. Splash screen appears
 * 2. After 3 seconds, try to show force interstitial ad
 * 3. If no fill (onNoAdAvailable) or error (onShowFailed) => pass to HomeScreen
 * 4. If ad shows successfully => user dismisses it => pass to HomeScreen
 */

// In SplashActivity or SplashViewModel:

import androidx.lifecycle.lifecycleScope
import com.tinhtx.baseads.interstitial.InterstitialAdManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {
    
    @Inject
    lateinit var interstitialAdManager: InterstitialAdManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        lifecycleScope.launch {
            // Wait 3 seconds before showing interstitial
            delay(3000)
            
            showInterstitialAndNavigate()
        }
    }
    
    private fun showInterstitialAndNavigate() {
        val adWasShown = interstitialAdManager.show(
            activity = this,
            onShown = {
                // Ad was dismissed successfully after showing
                // Navigate to HomeScreen
                navigateToHomeScreen()
            },
            onNoAdAvailable = {
                // No ad available (no fill)
                // Pass to HomeScreen immediately
                Log.d("SplashScreen", "No interstitial ad available, proceeding to HomeScreen")
                navigateToHomeScreen()
            },
            onShowFailed = { errorMessage ->
                // Ad failed to show
                // Pass to HomeScreen immediately
                Log.e("SplashScreen", "Interstitial ad failed to show: $errorMessage")
                navigateToHomeScreen()
            }
        )
        
        // If show() returned false immediately (checked and found no ad)
        // The callback will be called, so we don't need to do anything here
    }
    
    private fun navigateToHomeScreen() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}

/**
 * Alternative approach using suspend function for better coroutine handling:
 */
class SplashViewModelAlternative(
    private val interstitialAdManager: InterstitialAdManager
) : ViewModel() {
    
    fun startSplashFlow(activity: Activity) {
        viewModelScope.launch {
            // Wait 3 seconds
            delay(3000)
            
            // Show ad and wait for result
            val adWasShown = interstitialAdManager.show(
                activity = activity,
                onShown = {
                    navigateToHome()
                },
                onNoAdAvailable = {
                    navigateToHome()
                },
                onShowFailed = { error ->
                    navigateToHome()
                }
            )
            
            // This will be executed immediately since show() is non-blocking
            // The actual navigation happens in the callbacks
        }
    }
    
    private fun navigateToHome() {
        // Navigate using your navigation method
    }
}

/**
 * What happens in each scenario:
 * 
 * SCENARIO 1: Ad is available
 * - show() checks cachedAd, finds ad
 * - Sets fullScreenContentCallback
 * - Calls ad.show(activity)
 * - User sees and dismisses ad
 * - onAdDismissedFullScreenContent is called
 * - onShown callback is invoked
 * - navigateToHomeScreen() is called
 * 
 * SCENARIO 2: No ad available (no fill)
 * - show() checks cachedAd, finds null
 * - Logs "No cached ad available"
 * - Updates status to NOT_AVAILABLE with "No fill"
 * - Calls onNoAdAvailable callback immediately
 * - navigateToHomeScreen() is called
 * - preload() is triggered for next time
 * 
 * SCENARIO 3: Ad show failed
 * - show() checks cachedAd, finds ad
 * - onAdFailedToShowFullScreenContent is called
 * - Calls onShowFailed callback with error message
 * - navigateToHomeScreen() is called
 * 
 * SCENARIO 4: Exception during show()
 * - show() checks cachedAd, finds ad
 * - Exception thrown during ad.show(activity)
 * - Catch block logs exception
 * - Calls onShowFailed callback with error message
 * - navigateToHomeScreen() is called
 */
