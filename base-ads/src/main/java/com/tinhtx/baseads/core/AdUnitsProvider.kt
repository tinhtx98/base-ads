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
}

/**
 * Production implementation example.
 * Apps should create their own implementation with real ad unit IDs.
 */
class ProductionAdUnitsProvider(
    private val bannerUnitId: String,
    private val interstitialUnitId: String
) : AdUnitsProvider {
    
    override val bannerAdUnitId: String = bannerUnitId
    override val interstitialAdUnitId: String = interstitialUnitId
}