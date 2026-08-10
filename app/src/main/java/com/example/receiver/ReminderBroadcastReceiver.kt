package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "⏰ Nhắc lịch họp cán bộ"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Bạn có cuộc họp sắp diễn ra trong 15 phút tới."
        val meetingId = intent.getIntExtra("EXTRA_MEETING_ID", 0)

        showNotification(context, title, message, meetingId)
    }

    private fun showNotification(context: Context, title: String, message: String, id: Int) {
        val channelId = "vhxh_meeting_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lịch họp UBND xã Liên Minh",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo nhắc nhở lịch họp và công việc"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            id,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}

