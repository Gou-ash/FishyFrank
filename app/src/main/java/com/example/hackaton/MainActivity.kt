package com.example.hackaton

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hackaton.ui.theme.HackatonTheme

class MainActivity : ComponentActivity() {


    private val notification = Notification(this)

    // Place state here so it survives recompositions and can be updated in onResume()
    private var usagePermissionGranted by mutableStateOf(false)
    private var tiktokMinutes by mutableStateOf(-1)
    private var askedUsageAccessThisSession = false
    val appUsageTimeManager = AppUsageTimeManager(this)


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()




        // Initial permission check
        usagePermissionGranted = hasUsageStatsPermission()
        if (!usagePermissionGranted && !askedUsageAccessThisSession) {
            askedUsageAccessThisSession = true
            launchUsageAccessSettings()
        }

        setContentView(R.layout.main_activity)

        val settingsActivityButton = findViewById<ImageButton>(R.id.TimeSettingsButton)
        settingsActivityButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val rightActivityButton = findViewById<ImageButton>(R.id.rightActivity)
        rightActivityButton.setOnClickListener {
            val intent = Intent(this, ScrollingActivity::class.java)
            startActivity(intent)
        }

        val leftActivityButton = findViewById<ImageButton>(R.id.HomeTimeButton)
        leftActivityButton.setOnClickListener {
            val intent = Intent(this, time_per_app_activity::class.java)
            startActivity(intent)
        }

//        val homeTimeButton = findViewById<ImageButton>(R.id.HomeTimeButton)
//        homeTimeButton.setOnClickListener {
//            val intent = Intent(this, TimeActivity::class.java)
//            startActivity(intent)
//        }



        notification.createNotificationChannel()
    }

    // On returning to the app, check if the permission was granted and update usage
    override fun onResume() {
        super.onResume()
        val permissionNow = hasUsageStatsPermission()
        if (permissionNow != usagePermissionGranted) {
            usagePermissionGranted = permissionNow
            if (permissionNow) {
                tiktokMinutes = appUsageTimeManager.getAppUsageMinutes("com.zhiliaoapp.musically")
            }
        } else if (permissionNow) {

            tiktokMinutes = appUsageTimeManager.getAppUsageMinutes("com.zhiliaoapp.musically")

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
}
