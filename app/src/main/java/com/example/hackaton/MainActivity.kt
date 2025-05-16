package com.example.hackaton

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.hackaton.ui.theme.HackatonTheme

class MainActivity : ComponentActivity() {

    private val notification = Notification(this)

    // Place state here so it survives recompositions and can be updated in onResume()
    private var usagePermissionGranted by mutableStateOf(false)
    private var tiktokMinutes by mutableStateOf(-1)
    private var askedUsageAccessThisSession = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initial permission check
        usagePermissionGranted = hasUsageStatsPermission()
        if (!usagePermissionGranted && !askedUsageAccessThisSession) {
            askedUsageAccessThisSession = true
            launchUsageAccessSettings()
        } else if (usagePermissionGranted) {
            tiktokMinutes = getAppUsageMinutes("com.zhiliaoapp.musically")
        }

        setContent {
            HackatonTheme {
                var notificationRequested by remember { mutableStateOf(false) }

                // Compose will recompose when usagePermissionGranted or tiktokMinutes changes
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GreetingWithButtonAndUsage(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        onNotifyClick = {
                            notificationRequested = true
                        },
                        onRequestUsagePermission = {
                            launchUsageAccessSettings()
                        },
                        usagePermissionGranted = usagePermissionGranted,
                        tiktokMinutes = tiktokMinutes
                    )
                }
                // React to notification button press
                if (notificationRequested) {
                    notificationRequested = false
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notification.showNotification(
                            tiktokMinutes.takeIf { it >= 0 } ?: 15,
                            "TikTok"
                        )
                    }
                }
            }
        }
        notification.createNotificationChannel()
    }

    // On returning to the app, check if the permission was granted and update usage
    override fun onResume() {
        super.onResume()
        val permissionNow = hasUsageStatsPermission()
        if (permissionNow != usagePermissionGranted) {
            usagePermissionGranted = permissionNow
            if (permissionNow) {
                tiktokMinutes = getAppUsageMinutes("com.zhiliaoapp.musically")
            }
        } else if (permissionNow) {
            // Always refresh usage
            tiktokMinutes = getAppUsageMinutes("com.zhiliaoapp.musically")
        }
    }

    /** Checks if Usage Stats permission is granted */
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Binder.getCallingUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** Launches the Usage Access Settings screen */
    private fun launchUsageAccessSettings() {
        try {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Cannot open Usage Access Settings. Please grant permission manually.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** Returns minutes spent in a given app in the last 24 hours */
    private fun getAppUsageMinutes(appPackage: String): Int {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000L * 60 * 60 * 24 // 24 hours ago
        val stats: List<UsageStats> =
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
        val usage = stats.find { it.packageName == appPackage }
        return if (usage != null) (usage.totalTimeInForeground / 60000).toInt() else 0
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notification.showNotification(15, "TikTok")
        } else {
            Toast.makeText(
                applicationContext, "Permission denied",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

@Composable
fun GreetingWithButtonAndUsage(
    name: String,
    modifier: Modifier = Modifier,
    onNotifyClick: () -> Unit,
    onRequestUsagePermission: () -> Unit,
    usagePermissionGranted: Boolean,
    tiktokMinutes: Int
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hello $name!",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = onNotifyClick) {
            Text("Send Notification")
        }
        Spacer(Modifier.height(24.dp))
        if (!usagePermissionGranted) {
            Text("Usage access permission is required to show TikTok usage time.")
            Button(onClick = onRequestUsagePermission) {
                Text("Grant Usage Access")
            }
        } else {
            if (tiktokMinutes >= 0) {
                Text("Time spent in TikTok in last 24h: $tiktokMinutes minutes")
            } else {
                Text("Loading TikTok usage...")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingWithButtonAndUsagePreview() {
    HackatonTheme {
        GreetingWithButtonAndUsage(
            name = "Android",
            onNotifyClick = {},
            onRequestUsagePermission = {},
            usagePermissionGranted = false,
            tiktokMinutes = -1
        )
    }
}