package com.example.hackaton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.hackaton.ui.theme.HackatonTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HackatonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NotificationTestUI(
                        modifier = Modifier.padding(innerPadding),
                        sendNotification = {
                            // Request notification permission if needed (Android 13+)
                            NotificationHelper.requestNotificationPermission(this)

                            // Create NotificationHelper and send a notification
                            val helper = NotificationHelper(this)
                            helper.sendNotification(
                                notificationId = 1,
                                title = "Hello from Hackaton!",
                                text = "If you see this, notifications work 🎉"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationTestUI(
    modifier: Modifier = Modifier,
    sendNotification: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Test Notifications",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(onClick = { sendNotification() }) {
            Text("Send Notification")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationTestUIPreview() {
    HackatonTheme {
        NotificationTestUI(sendNotification = {})
    }
}