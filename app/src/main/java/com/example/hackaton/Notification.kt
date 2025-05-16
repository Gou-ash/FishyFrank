//package com.example.hackaton
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.core.app.ActivityCompat
//import android.content.pm.PackageManager
//import android.Manifest
//
//class NotificationHelper(private val context: Context) {
//
//    companion object {
//        const val DEFAULT_CHANNEL_ID = "default_channel"
//
//        // Call this from your Activity to request permission (Android 13+)
//        fun requestNotificationPermission(activity: android.app.Activity, requestCode: Int = 1001) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                if (ActivityCompat.checkSelfPermission(
//                        activity,
//                        Manifest.permission.POST_NOTIFICATIONS
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    ActivityCompat.requestPermissions(
//                        activity,
//                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//                        requestCode
//                    )
//                }
//            }
//        }
//    }
//
//    // Should be called before sending any notifications
//    fun createChannel(
//        channelId: String = DEFAULT_CHANNEL_ID,
//        name: String = context.getString(R.string.channel_name),
//        descriptionText: String = context.getString(R.string.channel_description)
//    ) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val mChannel = NotificationChannel(channelId, name, importance)
//            mChannel.description = descriptionText
//            val notificationManager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(mChannel)
//        }
//    }
//
//    // Builds a notification with dynamic title and text
//    private fun getNotificationBuilder(
//        channelId: String,
//        title: String,
//        text: String
//    ): NotificationCompat.Builder {
//        return NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use your own icon
//            .setContentTitle(title)
//            .setContentText(text)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setAutoCancel(true)
//    }
//
//    // Sends a notification (call createChannel() before using this)
//    fun sendNotification(
//        channelId: String = DEFAULT_CHANNEL_ID,
//        notificationId: Int,
//        title: String,
//        text: String
//    ) {
//        // Android 13+ permission check
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.POST_NOTIFICATIONS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // Permission not granted, handle in Activity
//                return
//            }
//        }
//
//        // Ensure channel exists (safe for repeated calls)
//        createChannel(channelId)
//
//        val builder = getNotificationBuilder(channelId, title, text)
//        with(NotificationManagerCompat.from(context)) {
//            notify(notificationId, builder.build())
//        }
//    }
//}