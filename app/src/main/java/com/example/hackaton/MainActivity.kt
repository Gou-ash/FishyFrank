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
    val CHANNEL_ID = "ch1"
    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    companion object {
        const val ACTION_DISMISS = "com.example.hackaton.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.example.hackaton.ACTION_SNOOZE"
    }

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
                        showNotification()
                    }
                }
            }
        }
        createNotificationChannel()
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showNotification()
        } else {
            Toast.makeText(
                applicationContext, "Permission denied",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification() {
        // Set intents and pending intents to call service on click of "dismiss" action button of notification
        val dismissIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_DISMISS
        }
        val piDismiss = PendingIntent.getService(
            this, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Set intents and pending intents to call service on click of "snooze" action button of notification
        val snoozeIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_SNOOZE
        }
        val piSnooze = PendingIntent.getService(
            this, 1, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("test")
            .setContentText("test2")
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                R.drawable.ic_launcher_background, // Replace with your dismiss icon
                "Dismiss",
                piDismiss
            )
            .addAction(
                R.drawable.ic_launcher_background, // Replace with your snooze icon
                "Snooze",
                piSnooze
            )

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "KANAŁ"
            val descriptionText = "OPIS"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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