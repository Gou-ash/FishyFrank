package com.example.hackaton

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.hackaton.ui.theme.HackatonTheme

class MainActivity : ComponentActivity() {

    private val notification = Notification(this)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            HackatonTheme {

                var notificationRequested by remember { mutableStateOf(false) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GreetingWithButton(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        onNotifyClick = {
                            notificationRequested = true
                        }
                    )
                }
                // React to button press
                if (notificationRequested) {
                    notificationRequested = false
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notification.showNotification(15, "TikTok")
                    }
                }
            }
        }
        notification.createNotificationChannel()
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
fun GreetingWithButton(name: String, modifier: Modifier = Modifier, onNotifyClick: () -> Unit) {
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
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingWithButtonPreview() {
    HackatonTheme {
        GreetingWithButton("Android", onNotifyClick = {})
    }
}