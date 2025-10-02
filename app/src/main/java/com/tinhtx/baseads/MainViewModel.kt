/*
 * Base Ads Sample - Main ViewModel
 * 
 * ViewModel for the sample app demonstrating Base Ads integration
 * with VIP status management and debug information.
 */

package com.tinhtx.baseads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.baseads.core.AdUnitsProvider
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.TestVipGate
import com.tinhtx.baseads.core.VipGate
import com.tinhtx.baseads.data.AdsPrefs
import com.tinhtx.baseads.interstitial.InterstitialAdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main sample screen
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val vipGate: VipGate,
    private val adsPrefs: AdsPrefs,
    private val interstitialAdManager: InterstitialAdManager,
    private val adsConfig: AdsConfig,
    private val adUnitsProvider: AdUnitsProvider
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        refreshState()
    }
    
    /**
     * Toggles VIP status if using TestVipGate
     */
    fun toggleVipStatus() {
        if (vipGate is TestVipGate) {
            val currentStatus = vipGate.isVip()
            vipGate.setVipStatus(!currentStatus)
            refreshState()
        }
    }
    
    /**
     * Refreshes the UI state with current values
     */
    fun refreshState() {
        viewModelScope.launch {
            val isVip = vipGate.isVip()
            val debugInfo = adsPrefs.getDebugInfo()
            val interstitialDebugInfo = interstitialAdManager.getDebugInfo()
            
            // Add ironSource configuration info
            val ironSourceInfo = mapOf(
                "ironSource Enabled" to adsConfig.isIronSourceEnabled(),
                "ironSource App Key" to (adsConfig.getIronSourceAppKeySafe()?.take(8)?.plus("...") ?: "Not Set"),
                "ironSource Logging" to adsConfig.enableIronSourceLogging
            )
            
            // Add AdMob ad unit information
            val adUnitsInfo = mapOf(
                "Banner Ad Unit" to adUnitsProvider.bannerAdUnitId.takeLast(10).let { "...${it}" },
                "Interstitial Ad Unit" to adUnitsProvider.interstitialAdUnitId.takeLast(10).let { "...${it}" },
                "Using Test Ads" to adUnitsProvider.bannerAdUnitId.contains("3940256099942544"),
                "Ads Initialized" to true,
                "Network Status" to "Check connection"
            )
            
            _uiState.value = _uiState.value.copy(
                isVip = isVip,
                canToggleVip = vipGate is TestVipGate,
                debugInfo = debugInfo + interstitialDebugInfo + ironSourceInfo + adUnitsInfo
            )
        }
    }
    
    /**
     * Preloads interstitial ad
     */
    fun preloadAd() {
        interstitialAdManager.preload()
        refreshState()
    }
    
    /**
     * Clears ads cache for testing
     */
    fun clearAdsCache() {
        interstitialAdManager.clearCache()
        refreshState()
    }
    
    /**
     * Clears all ads preferences
     */
    fun clearAdsPrefs() {
        adsPrefs.clearAll()
        refreshState()
    }
}

/**
 * UI state for the main screen
 */
data class MainUiState(
    val isVip: Boolean = false,
    val canToggleVip: Boolean = false,
    val debugInfo: Map<String, Any> = emptyMap()
)