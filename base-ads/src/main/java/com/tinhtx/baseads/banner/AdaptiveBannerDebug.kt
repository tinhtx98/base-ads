package com.tinhtx.baseads.banner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinhtx.baseads.core.AdUnitsProvider
import com.tinhtx.baseads.core.AdsConfig
import com.tinhtx.baseads.core.AnalyticsLogger
import com.tinhtx.baseads.core.VipGate

/**
 * Debug version of AdaptiveBanner with visual indicators
 * Use this to debug layout and size issues
 * 
 * Features:
 * - Red background on container to show available space
 * - Blue background on banner to show actual ad size
 * - Size info overlay
 */
@Composable
fun AdaptiveBannerDebug(
    adUnitsProvider: AdUnitsProvider,
    adsConfig: AdsConfig,
    vipGate: VipGate,
    analyticsLogger: AnalyticsLogger,
    modifier: Modifier = Modifier,
    showDebugInfo: Boolean = true
) {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val screenWidthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
    
    Column(modifier = modifier) {
        // Debug info overlay
        if (showDebugInfo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2196F3).copy(alpha = 0.8f))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "🐛 Banner Debug Info",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Screen Width: ${screenWidthDp}dp (${displayMetrics.widthPixels}px)",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Red = Container | Blue = Banner Ad",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
        
        // Container with red background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.Red.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            // Banner with blue background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Blue.copy(alpha = 0.2f))
            ) {
                AdaptiveBanner(
                    adUnitsProvider = adUnitsProvider,
                    adsConfig = adsConfig,
                    vipGate = vipGate,
                    analyticsLogger = analyticsLogger,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Simple banner placeholder for preview/testing
 * Shows a colored box with "Banner Ad" text
 */
@Composable
fun BannerPlaceholder(
    modifier: Modifier = Modifier,
    heightDp: Int = 50
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Banner Ad Placeholder\n${heightDp}dp height",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}