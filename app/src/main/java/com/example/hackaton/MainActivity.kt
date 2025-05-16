package com.example.hackaton

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

data class TrackableApp(val name: String, val packageName: String)

val trackableApps = listOf(
    TrackableApp("TikTok", "com.zhiliaoapp.musically"),
    TrackableApp("Instagram", "com.instagram.android"),
    TrackableApp("Facebook", "com.facebook.katana"),
    TrackableApp("Twitter", "com.twitter.android"),
    TrackableApp("YouTube", "com.google.android.youtube"),
)

class MainActivity : ComponentActivity() {

    private val notification by lazy { Notification(this) }
    private val appUsageTimeManager by lazy { AppUsageTimeManager(this) }

    private var usagePermissionGranted by mutableStateOf(false)
    private var trackedApp by mutableStateOf(trackableApps[0])
    private var trackedAppMinutes by mutableStateOf(-1)
    private var askedUsageAccessThisSession = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initial permission check
        usagePermissionGranted = appUsageTimeManager.hasUsageStatsPermission()
        if (!usagePermissionGranted && !askedUsageAccessThisSession) {
            askedUsageAccessThisSession = true
            launchUsageAccessSettings()
        } else if (usagePermissionGranted) {
            trackedAppMinutes = appUsageTimeManager.getAppUsageMinutes(trackedApp.packageName)
        }

        setContent {
            HackatonTheme {
                var notificationRequested by remember { mutableStateOf(false) }

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
                        trackedApp = trackedApp,
                        trackedAppMinutes = trackedAppMinutes,
                        appList = trackableApps,
                        onAppChange = { newApp ->
                            trackedApp = newApp
                            trackedAppMinutes = appUsageTimeManager.getAppUsageMinutes(newApp.packageName)
                        }
                    )
                }

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
                            trackedAppMinutes.takeIf { it >= 0 } ?: 0,
                            trackedApp.name
                        )
                    }
                }
            }
        }
        notification.createNotificationChannel()
    }

    override fun onResume() {
        super.onResume()
        val permissionNow = appUsageTimeManager.hasUsageStatsPermission()
        if (permissionNow != usagePermissionGranted) {
            usagePermissionGranted = permissionNow
            if (permissionNow) {
                trackedAppMinutes = appUsageTimeManager.getAppUsageMinutes(trackedApp.packageName)
            }
        } else if (permissionNow) {
            trackedAppMinutes = appUsageTimeManager.getAppUsageMinutes(trackedApp.packageName)
        }
    }

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

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notification.showNotification(15, trackedApp.name)
        } else {
            Toast.makeText(
                applicationContext, "Permission denied",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreetingWithButtonAndUsage(
    name: String,
    modifier: Modifier = Modifier,
    onNotifyClick: () -> Unit,
    onRequestUsagePermission: () -> Unit,
    usagePermissionGranted: Boolean,
    trackedApp: TrackableApp,
    trackedAppMinutes: Int,
    appList: List<TrackableApp>,
    onAppChange: (TrackableApp) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
        // App selector
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = trackedApp.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("App to track") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                appList.forEach { app ->
                    DropdownMenuItem(
                        text = { Text(app.name) },
                        onClick = {
                            expanded = false
                            onAppChange(app)
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNotifyClick) {
            Text("Send Notification")
        }
        Spacer(Modifier.height(24.dp))
        if (!usagePermissionGranted) {
            Text("Usage access permission is required to show usage time.")
            Button(onClick = onRequestUsagePermission) {
                Text("Grant Usage Access")
            }
        } else {
            if (trackedAppMinutes >= 0) {
                Text("Time spent in ${trackedApp.name} in last 24h: $trackedAppMinutes minutes")
            } else {
                Text("Loading usage...")
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
            trackedApp = trackableApps[0],
            trackedAppMinutes = -1,
            appList = trackableApps,
            onAppChange = {}
        )
    }
}