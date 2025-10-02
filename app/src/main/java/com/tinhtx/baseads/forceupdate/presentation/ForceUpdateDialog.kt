/*
 * Force Update - Dialog UI
 * 
 * Compose UI for force update dialog.
 * Non-dismissible dialog with single "Update" button that opens Play Store.
 */

package com.tinhtx.baseads.forceupdate.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.tinhtx.baseads.R

/**
 * Force update dialog composable
 * 
 * Features:
 * - Non-dismissible when shouldForceUpdate is true
 * - Opens Play Store on "Update" button click
 * - Falls back to browser if Play Store not available
 * - Uses custom title/message from remote config or defaults
 * 
 * @param state The force update UI state
 * @param onUpdateClick Callback when user clicks update button
 */
@Composable
fun ForceUpdateDialog(
    state: ForceUpdateState,
    onUpdateClick: () -> Unit
) {
    val context = LocalContext.current
    
    if (state.shouldForceUpdate) {
        AlertDialog(
            onDismissRequest = {
                // Prevent dismissal when force update is required
                // Empty lambda = user cannot dismiss by clicking outside or back button
            },
            title = {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Notify viewmodel that user clicked
                        onUpdateClick()
                        
                        // Open Play Store or browser
                        openPlayStore(context, state.storeUrl)
                    }
                ) {
                    Text(stringResource(R.string.force_update_button))
                }
            },
            dismissButton = null // No dismiss button for force update
        )
    }
}

/**
 * Open Play Store for app update
 * 
 * Priority:
 * 1. Use storeUrl from remote config if provided
 * 2. Try market:// URI to open Play Store app
 * 3. Fallback to https://play.google.com in browser if Play Store not available
 * 
 * @param context Android context
 * @param storeUrl Optional custom store URL from remote config
 */
private fun openPlayStore(context: android.content.Context, storeUrl: String?) {
    val packageName = context.packageName
    
    try {
        // Priority 1: Use custom store URL if provided
        if (!storeUrl.isNullOrBlank()) {
            Log.d("ForceUpdateDialog", "Opening custom store URL: $storeUrl")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        }
        
        // Priority 2: Try to open Play Store app with market:// URI
        Log.d("ForceUpdateDialog", "Opening Play Store app for package: $packageName")
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(marketIntent)
        
    } catch (e: ActivityNotFoundException) {
        // Priority 3: Fallback to browser if Play Store not available
        Log.d("ForceUpdateDialog", "Play Store not available, opening browser")
        try {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        } catch (e2: Exception) {
            Log.e("ForceUpdateDialog", "Failed to open store or browser", e2)
        }
    } catch (e: Exception) {
        Log.e("ForceUpdateDialog", "Failed to open Play Store", e)
    }
}
