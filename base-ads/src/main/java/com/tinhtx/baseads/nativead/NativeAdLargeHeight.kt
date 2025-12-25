/*
 * Base Ads Module - Native Ad Large Height Composable
 * 
 * Large native ad component for article end or standalone sections.
 * Maximum visibility and engagement, highest eCPM.
 */

package com.tinhtx.baseads.nativead

import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.tinhtx.baseads.core.AdsLogger

/**
 * Large height native ad component for article end or standalone sections.
 * Height: ~280dp with media view
 * 
 * @param nativeAdManager The native ad manager instance
 * @param position Position in list (for analytics)
 * @param route Current screen/route name
 * @param modifier Compose modifier
 * @param showShimmer Show shimmer loading effect
 * @param onAdLoaded Callback when ad is loaded
 * @param onAdFailed Callback when ad fails to load
 */
@Composable
fun NativeAdLargeHeight(
    nativeAdManager: NativeAdManager,
    position: Int = -1,
    route: String? = null,
    modifier: Modifier = Modifier,
    showShimmer: Boolean = true,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (String) -> Unit = {}
) {
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    val template = NativeAdTemplate.LARGE_HEIGHT
    
    LaunchedEffect(Unit) {
        isLoading = true
        when (val result = nativeAdManager.getAd(template)) {
            is NativeAdResult.Success -> {
                nativeAd = result.ad
                isLoading = false
                onAdLoaded()
            }
            is NativeAdResult.Error -> {
                isLoading = false
                hasError = true
                onAdFailed(result.message)
            }
            is NativeAdResult.VipUser,
            is NativeAdResult.Disabled -> {
                isLoading = false
                hasError = true
            }
            is NativeAdResult.Loading -> {
                // Still loading
            }
        }
    }
    
    DisposableEffect(nativeAd) {
        onDispose {
            nativeAd?.destroy()
        }
    }
    
    if (hasError) {
        // Don't show anything on error
        return
    }
    
    if (isLoading && showShimmer) {
        NativeAdLargeShimmer(modifier = modifier)
        return
    }
    
    nativeAd?.let { ad ->
        NativeAdLargeContent(
            nativeAd = ad,
            nativeAdManager = nativeAdManager,
            template = template,
            position = position,
            route = route,
            modifier = modifier
        )
    }
}

@Composable
private fun NativeAdLargeContent(
    nativeAd: NativeAd,
    nativeAdManager: NativeAdManager,
    template: NativeAdTemplate,
    position: Int,
    route: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Report impression when displayed
    LaunchedEffect(nativeAd) {
        nativeAdManager.reportImpression(nativeAd, template, position, route)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AndroidView(
            factory = { ctx ->
                NativeAdView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    
                    // Main container
                    val mainLayout = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    
                    // Media View (16:9 aspect ratio)
                    val mediaView = MediaView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            0
                        ).apply {
                            // Calculate height for 16:9 aspect ratio
                            val displayMetrics = ctx.resources.displayMetrics
                            val screenWidth = displayMetrics.widthPixels - 48.dpToPx(ctx) // padding
                            height = (screenWidth / 1.91f).toInt()
                        }
                    }
                    mainLayout.addView(mediaView)
                    this.mediaView = mediaView
                    
                    // Content container
                    val contentLayout = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        setPadding(16.dpToPx(ctx), 12.dpToPx(ctx), 16.dpToPx(ctx), 16.dpToPx(ctx))
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    
                    // Header row (icon + headline + ad badge)
                    val headerRow = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }
                    
                    // Icon
                    val iconView = ImageView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            40.dpToPx(ctx), 40.dpToPx(ctx)
                        ).apply {
                            marginEnd = 12.dpToPx(ctx)
                        }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    headerRow.addView(iconView)
                    
                    // Headline container
                    val headlineContainer = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            0,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }
                    
                    // Headline
                    val headlineView = TextView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        textSize = 16f
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        setTextColor(android.graphics.Color.parseColor("#212121"))
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                    headlineContainer.addView(headlineView)
                    
                    // Advertiser
                    val advertiserView = TextView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        textSize = 12f
                        setTextColor(android.graphics.Color.parseColor("#757575"))
                    }
                    headlineContainer.addView(advertiserView)
                    
                    headerRow.addView(headlineContainer)
                    
                    // Ad badge
                    val adBadge = TextView(ctx).apply {
                        text = "Ad"
                        textSize = 10f
                        setTextColor(android.graphics.Color.WHITE)
                        setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                        setPadding(12, 4, 12, 4)
                    }
                    headerRow.addView(adBadge)
                    
                    contentLayout.addView(headerRow)
                    
                    // Body
                    val bodyView = TextView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 8.dpToPx(ctx)
                        }
                        textSize = 14f
                        maxLines = 3
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        setTextColor(android.graphics.Color.parseColor("#424242"))
                    }
                    contentLayout.addView(bodyView)
                    
                    // Rating and store row
                    val ratingRow = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 8.dpToPx(ctx)
                        }
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }
                    
                    // Star rating
                    val ratingBar = RatingBar(ctx, null, android.R.attr.ratingBarStyleSmall).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginEnd = 8.dpToPx(ctx)
                        }
                        numStars = 5
                        setIsIndicator(true)
                    }
                    ratingRow.addView(ratingBar)
                    
                    // Store
                    val storeView = TextView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginEnd = 8.dpToPx(ctx)
                        }
                        textSize = 12f
                        setTextColor(android.graphics.Color.parseColor("#757575"))
                    }
                    ratingRow.addView(storeView)
                    
                    // Price
                    val priceView = TextView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        textSize = 12f
                        setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    }
                    ratingRow.addView(priceView)
                    
                    contentLayout.addView(ratingRow)
                    
                    // CTA Button
                    val ctaButton = Button(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            48.dpToPx(ctx)
                        ).apply {
                            topMargin = 12.dpToPx(ctx)
                        }
                        textSize = 14f
                        setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
                        setTextColor(android.graphics.Color.WHITE)
                    }
                    contentLayout.addView(ctaButton)
                    
                    mainLayout.addView(contentLayout)
                    addView(mainLayout)
                    
                    // Bind the ad
                    bindLargeNativeAd(
                        this, nativeAd,
                        iconView, headlineView, bodyView, advertiserView,
                        ratingBar, storeView, priceView, ctaButton
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun bindLargeNativeAd(
    adView: NativeAdView,
    nativeAd: NativeAd,
    iconView: ImageView,
    headlineView: TextView,
    bodyView: TextView,
    advertiserView: TextView,
    ratingBar: RatingBar,
    storeView: TextView,
    priceView: TextView,
    ctaButton: Button
) {
    // Media view is already set in factory
    
    // Icon
    nativeAd.icon?.let { icon ->
        iconView.setImageDrawable(icon.drawable)
        adView.iconView = iconView
        iconView.visibility = android.view.View.VISIBLE
    } ?: run {
        iconView.visibility = android.view.View.GONE
    }
    
    // Headline
    nativeAd.headline?.let { headline ->
        headlineView.text = headline
        adView.headlineView = headlineView
    }
    
    // Body
    nativeAd.body?.let { body ->
        bodyView.text = body
        adView.bodyView = bodyView
        bodyView.visibility = android.view.View.VISIBLE
    } ?: run {
        bodyView.visibility = android.view.View.GONE
    }
    
    // Advertiser
    nativeAd.advertiser?.let { advertiser ->
        advertiserView.text = advertiser
        adView.advertiserView = advertiserView
        advertiserView.visibility = android.view.View.VISIBLE
    } ?: run {
        advertiserView.visibility = android.view.View.GONE
    }
    
    // Star rating
    nativeAd.starRating?.let { rating ->
        ratingBar.rating = rating.toFloat()
        adView.starRatingView = ratingBar
        ratingBar.visibility = android.view.View.VISIBLE
    } ?: run {
        ratingBar.visibility = android.view.View.GONE
    }
    
    // Store
    nativeAd.store?.let { store ->
        storeView.text = store
        adView.storeView = storeView
        storeView.visibility = android.view.View.VISIBLE
    } ?: run {
        storeView.visibility = android.view.View.GONE
    }
    
    // Price
    nativeAd.price?.let { price ->
        priceView.text = price
        adView.priceView = priceView
        priceView.visibility = android.view.View.VISIBLE
    } ?: run {
        priceView.visibility = android.view.View.GONE
    }
    
    // CTA
    nativeAd.callToAction?.let { cta ->
        ctaButton.text = cta
        adView.callToActionView = ctaButton
    }
    
    // Register the ad
    adView.setNativeAd(nativeAd)
}

@Composable
fun NativeAdLargeShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Media placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.91f)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        // Headline placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Advertiser placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Body placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = 0.2f))
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = 0.2f))
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // CTA placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }
        }
    }
}

// Extension function to convert dp to pixels
private fun Int.dpToPx(context: android.content.Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}
