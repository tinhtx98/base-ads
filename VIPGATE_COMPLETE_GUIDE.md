# 🔐 VipGate Complete Guide
*BaseAds Library - VIP User Management System*

## 📋 Table of Contents
1. [Overview](#overview)
2. [Core Concepts](#core-concepts)
3. [Built-in Implementations](#built-in-implementations)
4. [Custom Implementation](#custom-implementation)
5. [Billing Integration](#billing-integration)
6. [Real-time Updates](#real-time-updates)
7. [UI Integration](#ui-integration)
8. [Testing](#testing)
9. [Best Practices](#best-practices)
10. [Troubleshooting](#troubleshooting)

---

## 🎯 Overview

**VipGate** là hệ thống quản lý người dùng VIP trong BaseAds library, cho phép:
- ✅ Tắt **tất cả quảng cáo** cho người dùng premium
- ✅ Tích hợp với **billing systems** (Google Play Billing, etc.)
- ✅ **Real-time updates** khi VIP status thay đổi
- ✅ **Automatic analytics tracking** cho VIP users
- ✅ **Seamless integration** với tất cả ad components

### 🔄 How It Works
```
User Purchases VIP → BillingManager Updates → VipGate.isVip() = true → All Ads Disabled
```

---

## 🏗️ Core Concepts

### Interface Definition
```kotlin
interface VipGate {
    /**
     * Returns true if the current user has VIP/premium status
     * and should not see any ads.
     */
    fun isVip(): Boolean
}
```

### Ad Disable Logic
Khi `vipGate.isVip()` returns `true`:
- 🚫 **InterstitialAdManager** skips all interstitial ads
- 🚫 **BannerPreloader** skips banner loading
- 🚫 **SmartAdHandler** bypasses ad logic
- ✅ **Analytics** automatically tracks VIP events

---

## 🛠️ Built-in Implementations

### 1. DefaultVipGate
```kotlin
/**
 * Default implementation - always returns false
 * Use as fallback or when VIP is not needed
 */
class DefaultVipGate @Inject constructor(
    private val adsPrefs: AdsPrefs
) : VipGate {
    override fun isVip(): Boolean = false
}
```

### 2. TestVipGate
```kotlin
/**
 * Test implementation - allows manual VIP toggle
 * Perfect for development and testing
 */
class TestVipGate(private var vipStatus: Boolean = false) : VipGate {
    
    override fun isVip(): Boolean = vipStatus
    
    fun setVipStatus(isVip: Boolean) {
        vipStatus = isVip
    }
}
```

---

## 🎨 Custom Implementation

### Basic Custom VipGate
```kotlin
class MyAppVipGate @Inject constructor(
    private val userRepository: UserRepository,
    private val sharedPreferences: SharedPreferences
) : VipGate {
    
    override fun isVip(): Boolean {
        return try {
            // Check from user data
            val user = userRepository.getCurrentUser()
            val hasVipFromServer = user?.isPremium == true
            
            // Check from local storage
            val hasVipLocal = sharedPreferences.getBoolean("user_is_vip", false)
            
            // Return true if either source confirms VIP
            hasVipFromServer || hasVipLocal
        } catch (e: Exception) {
            // Fallback to local storage on error
            sharedPreferences.getBoolean("user_is_vip", false)
        }
    }
}
```

### Advanced VipGate with Caching
```kotlin
class CachedVipGate @Inject constructor(
    private val userRepository: UserRepository,
    private val billingManager: BillingManager,
    private val adsPrefs: AdsPrefs
) : VipGate {
    
    private var cachedVipStatus: Boolean? = null
    private var lastCheck: Long = 0
    private val cacheTimeout = 5 * 60 * 1000L // 5 minutes
    
    override fun isVip(): Boolean {
        val now = System.currentTimeMillis()
        
        // Use cache if still valid
        if (cachedVipStatus != null && now - lastCheck < cacheTimeout) {
            return cachedVipStatus!!
        }
        
        // Refresh VIP status
        return refreshVipStatus()
    }
    
    private fun refreshVipStatus(): Boolean {
        return try {
            val vipStatus = checkMultipleSources()
            
            // Cache the result
            cachedVipStatus = vipStatus
            lastCheck = System.currentTimeMillis()
            
            // Store in preferences for persistence
            adsPrefs.setVipStatus(vipStatus)
            
            vipStatus
        } catch (e: Exception) {
            AdsLogger.e("VipGate", "Error checking VIP status", e)
            // Fallback to cached or stored value
            cachedVipStatus ?: adsPrefs.isVip()
        }
    }
    
    private fun checkMultipleSources(): Boolean {
        // Check billing manager first (most authoritative)
        if (billingManager.hasActiveSubscription()) {
            return true
        }
        
        // Check user repository
        val user = userRepository.getCurrentUser()
        if (user?.isPremium == true) {
            return true
        }
        
        // Check stored preferences
        if (adsPrefs.isVip()) {
            return true
        }
        
        return false
    }
    
    /**
     * Call this when VIP status might have changed
     */
    fun invalidateCache() {
        cachedVipStatus = null
        lastCheck = 0
    }
    
    /**
     * Force refresh VIP status
     */
    fun forceRefresh(): Boolean {
        invalidateCache()
        return isVip()
    }
}
```

---

## 💳 Billing Integration

### 1. BillingManager Setup
```kotlin
class BillingManager @Inject constructor(
    private val context: Context,
    private val vipGate: VipGate // Inject to update when purchase completes
) {
    
    private var billingClient: BillingClient? = null
    private var purchaseUpdateListener: PurchaseUpdatesListener? = null
    
    // VIP Product IDs
    companion object {
        const val VIP_MONTHLY = "vip_monthly"
        const val VIP_YEARLY = "vip_yearly"
        const val VIP_LIFETIME = "vip_lifetime"
    }
    
    fun initializeBilling() {
        purchaseUpdateListener = PurchaseUpdatesListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
        }
        
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchaseUpdateListener!!)
            .enablePendingPurchases()
            .build()
            
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Check existing purchases
                    checkExistingPurchases()
                }
            }
            
            override fun onBillingServiceDisconnected() {
                // Handle disconnection
            }
        })
    }
    
    fun hasActiveSubscription(): Boolean {
        return try {
            val purchasesResult = billingClient?.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            
            purchasesResult?.purchasesList?.any { purchase ->
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                listOf(VIP_MONTHLY, VIP_YEARLY, VIP_LIFETIME).contains(purchase.products.firstOrNull())
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // VIP purchase successful
            when (purchase.products.firstOrNull()) {
                VIP_MONTHLY, VIP_YEARLY, VIP_LIFETIME -> {
                    // Update VIP status
                    if (vipGate is CachedVipGate) {
                        vipGate.invalidateCache()
                    }
                    
                    // Acknowledge the purchase
                    acknowledgePurchase(purchase)
                    
                    // Notify UI
                    onVipPurchaseSuccess(purchase)
                }
            }
        }
    }
    
    private fun onVipPurchaseSuccess(purchase: Purchase) {
        // Send analytics event
        AnalyticsLogger.logEvent("vip_purchase_success", mapOf(
            "product_id" to purchase.products.firstOrNull(),
            "purchase_token" to purchase.purchaseToken
        ))
        
        // Show success UI
        // Trigger UI refresh
    }
}
```

### 2. BillingVipGate Implementation
```kotlin
class BillingVipGate @Inject constructor(
    private val billingManager: BillingManager,
    private val sharedPreferences: SharedPreferences
) : VipGate {
    
    companion object {
        private const val KEY_VIP_STATUS = "vip_status_cache"
        private const val KEY_LAST_CHECK = "vip_last_check"
        private const val CACHE_DURATION = 10 * 60 * 1000L // 10 minutes
    }
    
    override fun isVip(): Boolean {
        // Quick cache check
        val lastCheck = sharedPreferences.getLong(KEY_LAST_CHECK, 0)
        val now = System.currentTimeMillis()
        
        if (now - lastCheck < CACHE_DURATION) {
            return sharedPreferences.getBoolean(KEY_VIP_STATUS, false)
        }
        
        // Check billing manager
        return refreshFromBilling()
    }
    
    private fun refreshFromBilling(): Boolean {
        return try {
            val hasVip = billingManager.hasActiveSubscription()
            
            // Cache the result
            sharedPreferences.edit()
                .putBoolean(KEY_VIP_STATUS, hasVip)
                .putLong(KEY_LAST_CHECK, System.currentTimeMillis())
                .apply()
            
            hasVip
        } catch (e: Exception) {
            // Return cached value on error
            sharedPreferences.getBoolean(KEY_VIP_STATUS, false)
        }
    }
    
    /**
     * Call this when purchase is completed
     */
    fun onPurchaseCompleted() {
        sharedPreferences.edit()
            .remove(KEY_LAST_CHECK) // Force refresh
            .apply()
    }
}
```

---

## 🔄 Real-time Updates

### 1. VipGate with Flow
```kotlin
class ReactiveVipGate @Inject constructor(
    private val billingManager: BillingManager,
    private val userRepository: UserRepository
) : VipGate {
    
    private val _vipStatus = MutableStateFlow<Boolean?>(null)
    val vipStatusFlow: StateFlow<Boolean?> = _vipStatus.asStateFlow()
    
    init {
        // Initialize
        refreshVipStatus()
        
        // Listen to billing changes
        billingManager.purchaseUpdates.collect { 
            refreshVipStatus()
        }
        
        // Listen to user changes
        userRepository.userUpdates.collect {
            refreshVipStatus()
        }
    }
    
    override fun isVip(): Boolean {
        return _vipStatus.value ?: false
    }
    
    private suspend fun refreshVipStatus() {
        val hasVip = billingManager.hasActiveSubscription() ||
                    userRepository.getCurrentUser()?.isPremium == true
        
        _vipStatus.value = hasVip
    }
}
```

### 2. UI State Management
```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val vipGate: VipGate,
    private val billingManager: BillingManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        // Initial state
        refreshVipStatus()
        
        // Listen to billing updates if using ReactiveVipGate
        if (vipGate is ReactiveVipGate) {
            viewModelScope.launch {
                vipGate.vipStatusFlow.collect { isVip ->
                    if (isVip != null) {
                        _uiState.value = _uiState.value.copy(isVip = isVip)
                    }
                }
            }
        }
    }
    
    fun purchaseVip(productId: String) {
        viewModelScope.launch {
            try {
                val success = billingManager.purchaseProduct(productId)
                if (success) {
                    // VIP status will be updated automatically via flow
                    refreshVipStatus() // Force immediate refresh for non-reactive implementation
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun refreshVipStatus() {
        val isVip = vipGate.isVip()
        _uiState.value = _uiState.value.copy(isVip = isVip)
    }
}
```

---

## 📱 UI Integration

### 1. Premium Screen
```kotlin
@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = "Upgrade to VIP",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Current Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isVip) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (uiState.isVip) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (uiState.isVip) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (uiState.isVip) "✨ VIP Member" else "Regular User",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Text(
                    text = if (uiState.isVip) {
                        "Enjoying ad-free experience!"
                    } else {
                        "Upgrade to remove all ads"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (!uiState.isVip) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // VIP Benefits
            VipBenefitsSection()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Purchase Options
            VipPurchaseOptions(
                products = uiState.vipProducts,
                onPurchase = { productId ->
                    viewModel.purchaseVip(productId)
                },
                isLoading = uiState.isPurchasing
            )
        }
        
        // Debug info in debug builds
        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(16.dp))
            VipDebugSection(
                vipStatus = uiState.isVip,
                onToggleVip = { viewModel.toggleVipForTesting() }
            )
        }
    }
}

@Composable
private fun VipBenefitsSection() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌟 VIP Benefits",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val benefits = listOf(
                "🚫 No more ads - Complete ad-free experience",
                "⚡ Faster app performance",
                "🎯 Priority customer support",
                "🔄 Automatic cloud sync",
                "🎨 Exclusive themes and features"
            )
            
            benefits.forEach { benefit ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun VipPurchaseOptions(
    products: List<VipProduct>,
    onPurchase: (String) -> Unit,
    isLoading: Boolean
) {
    Column {
        Text(
            text = "Choose Your Plan",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        products.forEach { product ->
            VipProductCard(
                product = product,
                onPurchase = { onPurchase(product.productId) },
                isLoading = isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun VipProductCard(
    product: VipProduct,
    onPurchase: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onPurchase() },
        colors = CardDefaults.cardColors(
            containerColor = if (product.isPopular) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (product.isPopular) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge {
                            Text("POPULAR")
                        }
                    }
                }
                
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (product.savings.isNotEmpty()) {
                    Text(
                        text = product.savings,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = product.price,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                if (product.pricePerMonth.isNotEmpty()) {
                    Text(
                        text = product.pricePerMonth,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

### 2. Premium ViewModel
```kotlin
@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val vipGate: VipGate,
    private val billingManager: BillingManager,
    private val analyticsLogger: AnalyticsLogger
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()
    
    init {
        refreshState()
        loadVipProducts()
    }
    
    fun purchaseVip(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPurchasing = true)
            
            try {
                analyticsLogger.logEvent("vip_purchase_attempt", mapOf(
                    "product_id" to productId
                ))
                
                val success = billingManager.launchPurchaseFlow(productId)
                
                if (success) {
                    // Purchase flow started successfully
                    // Actual purchase completion will be handled by BillingManager
                    // and VipGate will be updated automatically
                } else {
                    // Purchase flow failed to start
                    analyticsLogger.logEvent("vip_purchase_failed", mapOf(
                        "product_id" to productId,
                        "error" to "failed_to_start"
                    ))
                }
            } catch (e: Exception) {
                analyticsLogger.logEvent("vip_purchase_error", mapOf(
                    "product_id" to productId,
                    "error" to e.message
                ))
            } finally {
                _uiState.value = _uiState.value.copy(isPurchasing = false)
            }
        }
    }
    
    fun toggleVipForTesting() {
        if (vipGate is TestVipGate) {
            vipGate.setVipStatus(!vipGate.isVip())
            refreshState()
        }
    }
    
    private fun refreshState() {
        val isVip = vipGate.isVip()
        _uiState.value = _uiState.value.copy(isVip = isVip)
        
        if (isVip) {
            analyticsLogger.logEvent("vip_status_check", mapOf(
                "is_vip" to true
            ))
        }
    }
    
    private fun loadVipProducts() {
        viewModelScope.launch {
            try {
                val products = billingManager.getAvailableVipProducts()
                _uiState.value = _uiState.value.copy(vipProducts = products)
            } catch (e: Exception) {
                // Handle error loading products
            }
        }
    }
}

data class PremiumUiState(
    val isVip: Boolean = false,
    val isPurchasing: Boolean = false,
    val vipProducts: List<VipProduct> = emptyList()
)

data class VipProduct(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    val pricePerMonth: String = "",
    val savings: String = "",
    val isPopular: Boolean = false
)
```

---

## 🧪 Testing

### 1. Testing VipGate
```kotlin
@RunWith(AndroidJUnit4::class)
class VipGateTest {
    
    @Test
    fun testVipGate_defaultImplementation_returnsFalse() {
        val mockAdsPrefs = mockk<AdsPrefs>()
        val vipGate = DefaultVipGate(mockAdsPrefs)
        
        assertFalse(vipGate.isVip())
    }
    
    @Test
    fun testVipGate_testImplementation_canToggle() {
        val vipGate = TestVipGate(false)
        
        assertFalse(vipGate.isVip())
        
        vipGate.setVipStatus(true)
        assertTrue(vipGate.isVip())
        
        vipGate.setVipStatus(false)
        assertFalse(vipGate.isVip())
    }
    
    @Test
    fun testVipGate_billingIntegration_respectsPurchases() {
        val mockBillingManager = mockk<BillingManager>()
        every { mockBillingManager.hasActiveSubscription() } returns true
        
        val vipGate = BillingVipGate(mockBillingManager, mockk())
        
        assertTrue(vipGate.isVip())
    }
    
    @Test
    fun testVipGate_cachingBehavior() {
        val mockBillingManager = mockk<BillingManager>()
        every { mockBillingManager.hasActiveSubscription() } returns true
        
        val vipGate = CachedVipGate(mockk(), mockBillingManager, mockk())
        
        // First call should check billing
        assertTrue(vipGate.isVip())
        verify(exactly = 1) { mockBillingManager.hasActiveSubscription() }
        
        // Second call should use cache
        assertTrue(vipGate.isVip())
        verify(exactly = 1) { mockBillingManager.hasActiveSubscription() }
    }
}
```

### 2. Integration Testing
```kotlin
@HiltAndroidTest
class VipGateIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var vipGate: VipGate
    
    @Inject
    lateinit var interstitialAdManager: InterstitialAdManager
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun testVipUser_noInterstitialAds() {
        // Set VIP status
        if (vipGate is TestVipGate) {
            vipGate.setVipStatus(true)
        }
        
        // Try to show interstitial
        val activity = mockk<Activity>()
        val shown = interstitialAdManager.maybeShow(activity)
        
        // Should not show ads for VIP user
        assertFalse(shown)
    }
}
```

---

## ✅ Best Practices

### 1. Performance Optimization
```kotlin
// ✅ DO: Cache VIP status
class OptimizedVipGate : VipGate {
    private var cachedStatus: Boolean? = null
    private var lastCheck: Long = 0
    
    override fun isVip(): Boolean {
        if (System.currentTimeMillis() - lastCheck > CACHE_TIMEOUT) {
            cachedStatus = checkVipStatus()
            lastCheck = System.currentTimeMillis()
        }
        return cachedStatus ?: false
    }
}

// ❌ DON'T: Make expensive calls every time
class ExpensiveVipGate : VipGate {
    override fun isVip(): Boolean {
        // This gets called frequently!
        return networkService.checkVipStatus() // Expensive!
    }
}
```

### 2. Error Handling
```kotlin
// ✅ DO: Handle errors gracefully
override fun isVip(): Boolean {
    return try {
        billingManager.hasActiveSubscription()
    } catch (e: Exception) {
        AdsLogger.e("VipGate", "Error checking VIP status", e)
        // Fallback to cached/stored value
        sharedPrefs.getBoolean("last_known_vip_status", false)
    }
}
```

### 3. State Synchronization
```kotlin
// ✅ DO: Sync state across components
class SyncedVipGate : VipGate {
    fun onPurchaseCompleted() {
        invalidateCache()
        notifyVipStatusChanged()
    }
    
    private fun notifyVipStatusChanged() {
        // Notify InterstitialAdManager
        // Notify BannerPreloader  
        // Notify UI components
    }
}
```

### 4. Dependency Injection
```kotlin
// ✅ DO: Bind in Hilt module
@Module
@InstallIn(SingletonComponent::class)
abstract class VipModule {
    
    @Binds
    @Singleton
    abstract fun bindVipGate(
        billingVipGate: BillingVipGate
    ): VipGate
}

// ✅ DO: Provide configuration
@Provides
@Singleton
fun provideVipGate(
    billingManager: BillingManager,
    userRepo: UserRepository
): VipGate = CachedVipGate(billingManager, userRepo)
```

---

## 🔧 Troubleshooting

### Common Issues

#### 1. VIP Status Not Updating After Purchase
```kotlin
// Problem: VipGate still returns false after successful purchase

// Solution: Invalidate cache after purchase
class BillingManager {
    fun onPurchaseSuccess(purchase: Purchase) {
        // Clear VipGate cache
        if (vipGate is CachedVipGate) {
            vipGate.invalidateCache()
        }
        
        // Force UI refresh
        eventBus.post(VipStatusChangedEvent())
    }
}
```

#### 2. Ads Still Showing for VIP Users
```kotlin
// Check VipGate integration in InterstitialAdManager
private fun shouldShowAd(): Boolean {
    if (vipGate.isVip()) {
        AdsLogger.d("Interstitial", "Skipping - VIP user")
        return false // Make sure this returns false!
    }
    // ... other checks
}
```

#### 3. VIP Status Inconsistent Across App
```kotlin
// Solution: Use single source of truth
@Singleton
class VipStatusManager @Inject constructor() {
    private val _vipStatus = MutableLiveData<Boolean>()
    val vipStatus: LiveData<Boolean> = _vipStatus
    
    fun updateVipStatus(isVip: Boolean) {
        _vipStatus.value = isVip
    }
}
```

### Debug Tips

#### 1. Add Logging
```kotlin
class DebugVipGate : VipGate {
    override fun isVip(): Boolean {
        val result = actualVipCheck()
        AdsLogger.d("VipGate", "VIP status check: $result")
        return result
    }
}
```

#### 2. Debug UI
```kotlin
@Composable
fun VipDebugPanel(vipGate: VipGate) {
    if (BuildConfig.DEBUG) {
        Card {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("VIP Debug Info")
                Text("Status: ${vipGate.isVip()}")
                Text("Type: ${vipGate::class.simpleName}")
                
                if (vipGate is TestVipGate) {
                    Button(onClick = { 
                        vipGate.setVipStatus(!vipGate.isVip()) 
                    }) {
                        Text("Toggle VIP")
                    }
                }
            }
        }
    }
}
```

---

## 📋 Implementation Checklist

### Setup Checklist
- [ ] Choose VipGate implementation (Default/Test/Custom/Billing)
- [ ] Configure Hilt dependency injection
- [ ] Integrate with billing system (if needed)
- [ ] Add error handling and fallbacks
- [ ] Implement caching for performance
- [ ] Add analytics tracking

### Testing Checklist  
- [ ] Test VIP status detection
- [ ] Test ad disabling for VIP users
- [ ] Test purchase flow integration
- [ ] Test cache invalidation
- [ ] Test error scenarios
- [ ] Test UI state updates

### Production Checklist
- [ ] Replace TestVipGate with production implementation
- [ ] Configure real billing products
- [ ] Add monitoring and analytics
- [ ] Test edge cases (network issues, etc.)
- [ ] Document custom implementation for team

---

## 🎯 Summary

VipGate provides a clean, flexible way to manage VIP users in your app:

1. **🎨 Simple Interface** - Just implement `isVip(): Boolean`
2. **🔧 Flexible Implementation** - Choose from built-in or create custom
3. **💳 Billing Integration** - Seamless Google Play Billing support  
4. **⚡ Performance Optimized** - Built-in caching and error handling
5. **🧪 Testing Friendly** - TestVipGate for development and testing
6. **📊 Analytics Ready** - Automatic VIP event tracking

**Quick Start:**
```kotlin
// 1. Choose implementation
@Provides @Singleton
fun provideVipGate(): VipGate = BillingVipGate(billingManager)

// 2. Inject and use
@Inject lateinit var vipGate: VipGate

// 3. Check VIP status  
if (vipGate.isVip()) {
    // Ad-free experience
} else {
    // Show ads
}
```

**That's it!** BaseAds handles the rest automatically. 🚀

---

*For more details, see the [BaseAds Documentation](README.md) or check out the [sample app implementation](app/src/main/java/com/tinhtx/baseads/).*