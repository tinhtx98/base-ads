/*
 * Base Ads Module - Ad Units Provider
 * 
 * Interface for providing ad unit IDs and test implementation
 * for development purposes.
 */

package com.tinhtx.baseads.core

import javax.inject.Inject

/**
 * Interface for providing ad unit IDs.
 * Apps should implement this interface to provide their actual ad unit IDs.
 */
interface AdUnitsProvider {
    
    /**
     * Returns the ad unit ID for banner ads
     */
    val bannerAdUnitId: String
    
    /**
     * Returns the ad unit ID for interstitial ads
     */
    val interstitialAdUnitId: String
    
    /**
     * Returns the ad unit ID for native ads
     */
    val nativeAdUnitId: String
    
    /**
     * Returns the ad unit ID for open app ads
     */
    val openAppAdUnitId: String
}

/**
 * Test implementation of AdUnitsProvider using Google's test ad unit IDs.
 * Safe to use during development and testing.
 */
class TestAdUnitsProvider @Inject constructor() : AdUnitsProvider {
    
    override val bannerAdUnitId: String
        get() = AdsConstants.TestAdUnits.BANNER
    
    override val interstitialAdUnitId: String
        get() = AdsConstants.TestAdUnits.INTERSTITIAL
    
    override val nativeAdUnitId: String
        get() = AdsConstants.TestAdUnits.NATIVE
    
    override val openAppAdUnitId: String
        get() = AdsConstants.TestAdUnits.OPEN_APP
}

/**
 * Production implementation example.
 * Apps should create their own implementation with real ad unit IDs.
 */
class ProductionAdUnitsProvider(
    private val bannerUnitId: String,
    private val interstitialUnitId: String,
    private val nativeUnitId: String = AdsConstants.TestAdUnits.NATIVE,
    private val openAppUnitId: String = AdsConstants.TestAdUnits.OPEN_APP
) : AdUnitsProvider {
    
    override val bannerAdUnitId: String = bannerUnitId
    override val interstitialAdUnitId: String = interstitialUnitId
    override val nativeAdUnitId: String = nativeUnitId
    override val openAppAdUnitId: String = openAppUnitId
}