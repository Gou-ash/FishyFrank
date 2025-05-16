package com.example.hackaton

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.hackaton.ui.theme.HackatonTheme


    class MainActivity : ComponentActivity() {
        val CHANNEL_ID = "ch1"
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
                HackatonTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
            createNotificationChannel()
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
                ) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                return
            } else {
                showNotification()
            }
        }
        private val notificationPermissionLauncher =registerForActivityResult(
            ActivityResultContracts
                .RequestPermission()) { isGranted ->
            if(isGranted) {
                    showNotification()
            }else{
                Toast.makeText(
                    applicationContext, "Permission denied",
                    Toast.LENGTH_LONG).show()
            }
        }

        @SuppressLint("MissingPermission")
        private fun showNotification(){
            var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("test")
                .setContentText("test2")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            with(NotificationManagerCompat.from(this)) {


                // notificationId is a unique int for each notification that you must define.
                notify(1, builder.build())
            }
        }
        private fun createNotificationChannel() {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is not in the Support Library.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "KANAŁ"
                val descriptionText = "OPIS"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system.
                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    @Composable
    fun greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        HackatonTheme {
            greeting("Android")
        }
    }
