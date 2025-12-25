/*
 * Base Ads Module - Native Ad Inline (Small) Composable
 * 
 * Compact native ad component for feed items and list spacing.
 * Optimized for minimal intrusion while maintaining visibility.
 */

package com.tinhtx.baseads.nativead

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.tinhtx.baseads.core.AdsLogger

/**
 * Inline small native ad component for feed/list integration.
 * Height: ~100dp
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
fun NativeAdInline(
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
    
    val template = NativeAdTemplate.INLINE_SMALL
    
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
        NativeAdInlineShimmer(modifier = modifier)
        return
    }
    
    nativeAd?.let { ad ->
        NativeAdInlineContent(
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
private fun NativeAdInlineContent(
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AndroidView(
            factory = { ctx ->
                // Create NativeAdView programmatically
                NativeAdView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    
                    // Create inline layout
                    val contentLayout = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        setPadding(24, 16, 24, 16)
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    
                    // Icon
                    var iconView = ImageView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(48.dpToPx(ctx), 48.dpToPx(ctx)).apply {
                            marginEnd = 12.dpToPx(ctx)
                        }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    contentLayout.addView(iconView)
                    iconView = iconView
                    
                    // Text container
                    val textContainer = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            0,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }
                    
                    // Ad badge
                    val adBadge = TextView(ctx).apply {
                        text = "Ad"
                        textSize = 10f
                        setTextColor(android.graphics.Color.WHITE)
                        setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                        setPadding(8, 2, 8, 2)
                    }
                    textContainer.addView(adBadge)
                    
                    // Headline
                    var headlineView = TextView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        textSize = 14f
                        maxLines = 1
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        setTextColor(android.graphics.Color.parseColor("#212121"))
                    }
                    textContainer.addView(headlineView)
                    headlineView = headlineView
                    
                    // Body
                    var bodyView = TextView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        textSize = 12f
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        setTextColor(android.graphics.Color.parseColor("#757575"))
                    }
                    textContainer.addView(bodyView)
                    bodyView = bodyView
                    
                    contentLayout.addView(textContainer)
                    
                    // CTA Button
                    val ctaButton = Button(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            36.dpToPx(ctx)
                        ).apply {
                            marginStart = 8.dpToPx(ctx)
                        }
                        textSize = 12f
                        setPadding(16, 0, 16, 0)
                    }
                    contentLayout.addView(ctaButton)
                    callToActionView = ctaButton
                    
                    addView(contentLayout)
                    
                    // Bind the ad
                    bindNativeAd(this, nativeAd, iconView, headlineView, bodyView, ctaButton)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun bindNativeAd(
    adView: NativeAdView,
    nativeAd: NativeAd,
    iconView: ImageView,
    headlineView: TextView,
    bodyView: TextView,
    ctaButton: Button
) {
    // Icon
    nativeAd.icon?.let { icon ->
        iconView.setImageDrawable(icon.drawable)
        adView.iconView = iconView
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
fun NativeAdInlineShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Headline placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Body placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = 0.2f))
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // CTA placeholder
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }
    }
}

// Extension function to convert dp to pixels
private fun Int.dpToPx(context: android.content.Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}
