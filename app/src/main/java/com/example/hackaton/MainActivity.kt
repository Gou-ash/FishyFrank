package com.example.hackaton

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.hackaton.ui.theme.HackatonTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val CHANNEL_ID = "my_channel_id"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission if on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        // Create notification channel
        val notificationHelper = NotificationHelper(this)
        notificationHelper.createChannel(CHANNEL_ID)

        setContent {
            HackatonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Show a button to send notification
                    NotificationDemo(
                        modifier = Modifier.padding(innerPadding),
                        onSendNotif = {
                            notificationHelper.sendNotification(CHANNEL_ID, NOTIFICATION_ID)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationDemo(modifier: Modifier = Modifier, onSendNotif: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Hello Android!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onSendNotif() }) {
            Text("Send Notification")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationDemoPreview() {
    HackatonTheme {
        NotificationDemo(onSendNotif = {})
    }
}