/*
 * Base Ads Sample - Main Activity
 * 
 * Sample activity demonstrating complete integration of Base Ads module
 * with navigation, banner ads, interstitial ads, and VIP functionality.
 */

package com.tinhtx.baseads

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tinhtx.baseads.banner.AdaptiveBanner
import com.tinhtx.baseads.banner.BannerAdItem
import com.tinhtx.baseads.banner.BannerAdCard
import com.tinhtx.baseads.core.AdUnitsProvider
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.core.VipGate
import com.tinhtx.baseads.ext.MarkScreenOpened
import com.tinhtx.baseads.ext.navigateSmartSimple
import com.tinhtx.baseads.ext.smartClickableSimple
import com.tinhtx.baseads.forceupdate.presentation.ForceUpdateDialog
import com.tinhtx.baseads.forceupdate.presentation.ForceUpdateViewModel
import com.tinhtx.baseads.interstitial.InterstitialAdManager
import com.tinhtx.baseads.ui.theme.BaseAdsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var interstitialAdManager: InterstitialAdManager
    
    @Inject
    lateinit var analyticsLogger: AnalyticsLogger
    
    @Inject
    lateinit var adUnitsProvider: AdUnitsProvider
    
    @Inject
    lateinit var adsConfig: AdsConfig
    
    @Inject
    lateinit var vipGate: VipGate
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Preload interstitial ad
        interstitialAdManager.preload()
        
        setContent {
            BaseAdsTheme {
                // Force Update Dialog - Highest priority overlay
                val forceUpdateViewModel: ForceUpdateViewModel = hiltViewModel()
                val forceUpdateState by forceUpdateViewModel.state.collectAsState()
                
                // Show force update dialog if needed
                ForceUpdateDialog(
                    state = forceUpdateState,
                    onUpdateClick = { forceUpdateViewModel.onUpdateClicked() }
                )
                
                // Main app content
                SampleApp()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Refresh force update config when app resumes
        // This ensures we check for updates after user returns from Play Store
        val forceUpdateViewModel: ForceUpdateViewModel? = try {
            // Safe way to get ViewModel if available
            null // Will be handled by lifecycle owner in compose
        } catch (e: Exception) {
            null
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SampleApp() {
        val navController = rememberNavController()
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Base Ads Sample") }
                )
            },
            bottomBar = {
                // Show banner ad at bottom
                AdaptiveBanner(
                    adUnitsProvider = adUnitsProvider,
                    adsConfig = adsConfig,
                    vipGate = vipGate,
                    analyticsLogger = analyticsLogger
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("home") {
                    HomeScreen(navController = navController)
                }
                composable("details") {
                    DetailsScreen(navController = navController)
                }
                composable("settings") {
                    SettingsScreen(navController = navController)
                }
                composable("list") {
                    ListWithBannersScreen(navController = navController)
                }
            }
        }
    }

    @Composable
    fun HomeScreen(
        navController: NavHostController,
        viewModel: MainViewModel = hiltViewModel()
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        
        // Mark screen as opened for ads timing
        MarkScreenOpened(
            ads = interstitialAdManager,
            analytics = analyticsLogger,
            screenName = "home"
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // VIP Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "VIP Status",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isVip) "VIP User (Ads Disabled)" else "Regular User"
                        )
                        
                        if (uiState.canToggleVip) {
                            Switch(
                                checked = uiState.isVip,
                                onCheckedChange = { viewModel.toggleVipStatus() }
                            )
                        }
                    }
                }
            }
            
            // Navigation Buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Navigation (Smart Ads)",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    navController.navigateSmartSimple(
                                        route = "details",
                                        ads = interstitialAdManager,
                                        analytics = analyticsLogger,
                                        config = adsConfig,
                                        activity = this@MainActivity
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Go to Details")
                        }
                        
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    navController.navigateSmartSimple(
                                        route = "settings",
                                        ads = interstitialAdManager,
                                        analytics = analyticsLogger,
                                        config = adsConfig,
                                        activity = this@MainActivity
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Settings")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // New button to demo list with banners
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                navController.navigateSmartSimple(
                                    route = "list",
                                    ads = interstitialAdManager,
                                    analytics = analyticsLogger,
                                    config = adsConfig,
                                    activity = this@MainActivity
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("List with Banner Items")
                    }
                }
            }
            
            // Smart Clickable Examples
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Smart Clickable Examples",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Action Button (with ads)",
                        modifier = Modifier
                            .fillMaxWidth()
                            .smartClickableSimple(
                                label = "action_button",
                                ads = interstitialAdManager,
                                analytics = analyticsLogger,
                                config = adsConfig,
                                activity = this@MainActivity
                            ) {
                                // Action button click handler
                            }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // Ads Control
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Ads Control",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.preloadAd() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Preload Ad")
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.refreshState() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Refresh")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearAdsCache() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Cache")
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.clearAdsPrefs() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Prefs")
                        }
                    }
                }
            }
            
            // Debug Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Debug Information",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    uiState.debugInfo.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = value.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun DetailsScreen(navController: NavHostController) {
        // Mark screen as opened for ads timing
        MarkScreenOpened(
            ads = interstitialAdManager,
            analytics = analyticsLogger,
            screenName = "details"
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Details Screen",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "This is the details screen. Navigation to this screen may show an interstitial ad based on policy settings.",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text("Back to Home")
            }
        }
    }
    
    @Composable
    fun SettingsScreen(navController: NavHostController) {
        // Mark screen as opened for ads timing  
        MarkScreenOpened(
            ads = interstitialAdManager,
            analytics = analyticsLogger,
            screenName = "settings"
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Settings Screen",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "This screen is in the blocklist, so interstitial ads should NOT show when navigating here.",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text("Back to Home")
            }
        }
    }
    
    @Composable
    fun ListWithBannersScreen(navController: NavHostController) {
        // Mark screen as opened
        MarkScreenOpened(
            ads = interstitialAdManager,
            analytics = analyticsLogger,
            screenName = "list_with_banners"
        )
        
        // Sample data
        val items = remember {
            (1..20).map { "Item #$it - Sample Content" }
        }
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "List with Banner Items",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Button(
                        onClick = { navController.popBackStack() }
                    ) {
                        Text("Back")
                    }
                }
            }
            
            // List with banners as items
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Render items and banners with stable keys
                items.forEachIndexed { index, itemText ->
                    // Regular content item with stable key
                    item(key = "content_$index") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = itemText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "This is sample content for demonstration purposes. You can replace this with your actual content.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    // Insert banner every 5 items with stable key
                    if ((index + 1) % 5 == 0 && index < items.size - 1) {
                        val bannerNumber = (index + 1) / 5
                        
                        item(key = "banner_$bannerNumber") {
                            BannerAdItem(
                                adUnitsProvider = adUnitsProvider,
                                adsConfig = adsConfig,
                                vipGate = vipGate,
                                analyticsLogger = analyticsLogger,
                                topPadding = 16.dp,
                                bottomPadding = 16.dp,
                                showBackground = false,
                                adId = "banner_$bannerNumber"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun remember(calculation: () -> List<String>): List<String> {
    return androidx.compose.runtime.remember { calculation() }
}