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

// --- Trackable Apps Model & List ---
data class TrackableApp(val name: String, val packageName: String)

val trackableApps = listOf(
    TrackableApp("TikTok", "com.zhiliaoapp.musically"),
    TrackableApp("Instagram", "com.instagram.android"),
    TrackableApp("Facebook", "com.facebook.katana"),
    TrackableApp("Twitter", "com.twitter.android"),
    TrackableApp("YouTube", "com.google.android.youtube"),
)


// --- MainActivity ---
class MainActivity : ComponentActivity() {

    private val notification by lazy { Notification(this) }
    private val appUsageTimeManager by lazy { AppUsageTimeManager(this) }

    private lateinit var stepCounter: StepCounter
    private lateinit var stepStorage: StepStorage

    private var usagePermissionGranted by mutableStateOf(false)
    private var trackedApp by mutableStateOf(trackableApps[0])
    private var trackedAppMinutes by mutableStateOf(-1)
    private var askedUsageAccessThisSession = false

    // Persistent step tracking variables
    private var lastSensorValue by mutableStateOf(0L)
    private var totalSteps by mutableStateOf(0L)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        stepCounter = StepCounter(this)
        stepStorage = StepStorage(this)

        // Read last session state (lastSensorValue, totalSteps)
        val (storedLastSensorValue, storedTotalSteps) = stepStorage.readSession()
        lastSensorValue = storedLastSensorValue
        totalSteps = storedTotalSteps

        // Initial permission check
        usagePermissionGranted = appUsageTimeManager.hasUsageStatsPermission()
        if (!usagePermissionGranted && !askedUsageAccessThisSession) {
            askedUsageAccessThisSession = true
            launchUsageAccessSettings()
        } else if (usagePermissionGranted) {
            trackedAppMinutes = appUsageTimeManager.getAppUsageMinutes(trackedApp.packageName)
        }

        setContent {
            var notificationRequested by remember { mutableStateOf(false) }
            var stepsState by remember { mutableStateOf(totalSteps) }

            // Real-time step listening with persistent session logic
            DisposableEffect(Unit) {
                stepCounter.startListening { newSensorValue ->
                    // If first launch or after reboot, initialize lastSensorValue
                    if (lastSensorValue == 0L) {
                        lastSensorValue = newSensorValue
                    }
                    val delta = (newSensorValue - lastSensorValue)
                    if (delta > 0) {
                        totalSteps += delta
                        stepsState = totalSteps
                        lastSensorValue = newSensorValue
                        // Save to storage every update
                        stepStorage.saveSession(lastSensorValue, totalSteps)
                    }
                }
                onDispose {
                    stepCounter.stopListening()
                    // Save on dispose for safety
                    stepStorage.saveSession(lastSensorValue, totalSteps)
                }
            }

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
                    },
                    steps = stepsState,
                    isLoadingSteps = false,
                    onReloadSteps = {}
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
        notification.createNotificationChannel()
    }

    override fun onPause() {
        super.onPause()
        // Save session state on pause
        stepStorage.saveSession(lastSensorValue, totalSteps)
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

// --- UI: GreetingWithButtonAndUsage ---
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
    onAppChange: (TrackableApp) -> Unit,
    steps: Long,
    isLoadingSteps: Boolean,
    onReloadSteps: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text("Hi $name!")
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Tracked App:")
            Spacer(modifier = Modifier.width(8.dp))
            DropdownMenuBox(
                selectedApp = trackedApp,
                appList = appList,
                onAppChange = onAppChange
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (usagePermissionGranted) {
            Text("You've spent ${if (trackedAppMinutes >= 0) "$trackedAppMinutes min" else "--"} on ${trackedApp.name} today.")
        } else {
            Button(onClick = onRequestUsagePermission) {
                Text("Grant Usage Access")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNotifyClick) {
            Text("Notify Usage")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Steps counted this session: $steps")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onReloadSteps, enabled = !isLoadingSteps) {
            Text("Reload Steps")
        }
    }
}

@Composable
fun DropdownMenuBox(
    selectedApp: TrackableApp,
    appList: List<TrackableApp>,
    onAppChange: (TrackableApp) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedApp.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            appList.forEach { app ->
                DropdownMenuItem(
                    text = { Text(app.name) },
                    onClick = {
                        onAppChange(app)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingWithButtonAndUsageUsage() {
    GreetingWithButtonAndUsage(
        name = "Android",
        onNotifyClick = {},
        onRequestUsagePermission = {},
        usagePermissionGranted = false,
        trackedApp = trackableApps[0],
        trackedAppMinutes = -1,
        appList = trackableApps,
        onAppChange = {},
        steps = 0,
        isLoadingSteps = false,
        onReloadSteps = {}
    )
}