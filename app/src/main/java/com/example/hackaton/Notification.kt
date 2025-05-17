package com.example.hackaton

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService

class Notification(val context: Context) {
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


    @SuppressLint("MissingPermission")
    public fun showNotification(time:Int, appName:String ) {
        // Set intents and pending intents to call service on click of "dismiss" action button of notification
        val dismissIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_DISMISS
        }
        val piDismiss = PendingIntent.getService(
            context, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Set intents and pending intents to call service on click of "snooze" action button of notification
        val snoozeIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_SNOOZE
        }
        val piSnooze = PendingIntent.getService(
            context, 1, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Heeeej!")
            .setContentText("Spędziłeś już " + time + " minut w aplikacji " + appName + ", może czas na przerwę? Pobaw się ze mną!")
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

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }

    public fun createNotificationChannel() {
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
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}