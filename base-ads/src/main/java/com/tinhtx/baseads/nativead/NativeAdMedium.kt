/*
 * Base Ads Module - Native Ad Medium Composable
 * 
 * Medium native ad component with media view for better engagement.
 * Good balance between visibility and user experience.
 */

package com.tinhtx.baseads.nativead

import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
 * Medium native ad component with media view.
 * Height: ~150dp
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
fun NativeAdMedium(
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
    
    val template = NativeAdTemplate.INLINE_MEDIUM
    
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
        return
    }
    
    if (isLoading && showShimmer) {
        NativeAdMediumShimmer(modifier = modifier)
        return
    }
    
    nativeAd?.let { ad ->
        NativeAdMediumContent(
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
private fun NativeAdMediumContent(
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
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        AndroidView(
            factory = { ctx ->
                NativeAdView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    
                    // Main horizontal layout
                    val mainLayout = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        setPadding(12.dpToPx(ctx), 12.dpToPx(ctx), 12.dpToPx(ctx), 12.dpToPx(ctx))
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    
                    // Media View (square thumbnail)
                    val mediaView = MediaView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            100.dpToPx(ctx),
                            100.dpToPx(ctx)
                        ).apply {
                            marginEnd = 12.dpToPx(ctx)
                        }
                    }
                    mainLayout.addView(mediaView)
                    this.mediaView = mediaView
                    
                    // Content container
                    val contentLayout = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            0,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }
                    
                    // Header row (ad badge + advertiser)
                    val headerRow = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }
                    
                    // Ad badge
                    val adBadge = TextView(ctx).apply {
                        text = "Ad"
                        textSize = 9f
                        setTextColor(android.graphics.Color.WHITE)
                        setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                        setPadding(8, 2, 8, 2)
                    }
                    headerRow.addView(adBadge)
                    
                    // Advertiser
                    val advertiserView = TextView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginStart = 8.dpToPx(ctx)
                        }
                        textSize = 11f
                        setTextColor(android.graphics.Color.parseColor("#757575"))
                    }
                    headerRow.addView(advertiserView)
                    
                    contentLayout.addView(headerRow)
                    
                    // Headline
                    val headlineView = TextView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 4.dpToPx(ctx)
                        }
                        textSize = 14f
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        setTextColor(android.graphics.Color.parseColor("#212121"))
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                    contentLayout.addView(headlineView)
                    
                    // Body
                    val bodyView = TextView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 2.dpToPx(ctx)
                        }
                        textSize = 12f
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        setTextColor(android.graphics.Color.parseColor("#616161"))
                    }
                    contentLayout.addView(bodyView)
                    
                    // CTA Button
                    val ctaButton = Button(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            32.dpToPx(ctx)
                        ).apply {
                            topMargin = 6.dpToPx(ctx)
                        }
                        textSize = 11f
                        setPadding(20, 0, 20, 0)
                        setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
                        setTextColor(android.graphics.Color.WHITE)
                    }
                    contentLayout.addView(ctaButton)
                    
                    mainLayout.addView(contentLayout)
                    addView(mainLayout)
                    
                    // Bind the ad
                    bindMediumNativeAd(this, nativeAd, headlineView, bodyView, advertiserView, ctaButton)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun bindMediumNativeAd(
    adView: NativeAdView,
    nativeAd: NativeAd,
    headlineView: TextView,
    bodyView: TextView,
    advertiserView: TextView,
    ctaButton: Button
) {
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
    
    // CTA
    nativeAd.callToAction?.let { cta ->
        ctaButton.text = cta
        adView.callToActionView = ctaButton
    }
    
    // Register the ad
    adView.setNativeAd(nativeAd)
}

@Composable
fun NativeAdMediumShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Media placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Badge placeholder
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Headline placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Body placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = 0.2f))
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // CTA placeholder
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
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
