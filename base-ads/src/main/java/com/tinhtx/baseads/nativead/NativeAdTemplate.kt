/*
 * Base Ads Module - Native Ad Template Types
 * 
 * Enum defining different native ad template types for various use cases.
 */

package com.tinhtx.baseads.nativead

/**
 * Native ad template types for different display contexts.
 * 
 * @property heightDp Approximate height in dp
 * @property aspectRatio Recommended aspect ratio for media view
 */
enum class NativeAdTemplate(
    val heightDp: Int,
    val aspectRatio: Float,
    val showMediaView: Boolean
) {
    /**
     * Small inline template for feed items and list spacing.
     * Optimized for minimal intrusion while maintaining visibility.
     * 
     * Recommended use: Every 3-5 items in a feed
     * eCPM estimate: $2-6
     */
    INLINE_SMALL(
        heightDp = 100,
        aspectRatio = 4f,
        showMediaView = false
    ),
    
    /**
     * Medium template with media view for better engagement.
     * Good balance between visibility and user experience.
     * 
     * Recommended use: Every 5-8 items in a feed
     * eCPM estimate: $4-8
     */
    INLINE_MEDIUM(
        heightDp = 150,
        aspectRatio = 3f,
        showMediaView = true
    ),
    
    /**
     * Large height template for article end or standalone sections.
     * Maximum visibility and engagement, highest eCPM.
     * 
     * Recommended use: End of articles, standalone ad sections
     * eCPM estimate: $8-15
     */
    LARGE_HEIGHT(
        heightDp = 280,
        aspectRatio = 1.91f,
        showMediaView = true
    )
}

/**
 * Configuration for native ad display options.
 */
data class NativeAdOptions(
    val template: NativeAdTemplate = NativeAdTemplate.INLINE_MEDIUM,
    val showHeadline: Boolean = true,
    val showBody: Boolean = true,
    val showCallToAction: Boolean = true,
    val showIcon: Boolean = true,
    val showStarRating: Boolean = true,
    val showStore: Boolean = true,
    val showPrice: Boolean = true,
    val showAdvertiser: Boolean = true
)
