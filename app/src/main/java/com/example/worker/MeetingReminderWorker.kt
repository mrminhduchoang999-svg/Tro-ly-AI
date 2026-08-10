package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.R
import com.example.data.model.Meeting
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class MeetingReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val title = inputData.getString(KEY_TITLE) ?: "Nhắc lịch họp UBND xã"
            val message = inputData.getString(KEY_MESSAGE) ?: "Bạn có cuộc họp sắp diễn ra."
            val meetingId = inputData.getInt(KEY_MEETING_ID, 0)

            showNotification(context, title, message, meetingId)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun showNotification(context: Context, title: String, message: String, id: Int) {
        val channelId = "vhxh_meeting_reminders_workmanager"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở Lịch họp (WorkManager)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo nhắc nhở 15 phút trước khi cuộc họp bắt đầu"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(if (id != 0) id else (System.currentTimeMillis() % 10000).toInt(), notification)
    }

    companion object {
        const val KEY_TITLE = "KEY_TITLE"
        const val KEY_MESSAGE = "KEY_MESSAGE"
        const val KEY_MEETING_ID = "KEY_MEETING_ID"

        fun scheduleMeetingReminder(context: Context, meeting: Meeting) {
            if (meeting.isCompleted) return

            val reminderMinutes = if (meeting.reminderMinutes > 0) meeting.reminderMinutes else 15
            val dateTimeStr = "${meeting.date} ${meeting.startTime}"
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            try {
                val meetingTime = sdf.parse(dateTimeStr) ?: return
                val calendar = Calendar.getInstance().apply {
                    time = meetingTime
                    add(Calendar.MINUTE, -reminderMinutes)
                }

                val triggerMillis = calendar.timeInMillis
                val delayMillis = triggerMillis - System.currentTimeMillis()

                if (delayMillis <= 0) return

                val inputData = Data.Builder()
                    .putString(KEY_TITLE, "⏰ Sắp diễn ra cuộc họp (${reminderMinutes} phút nữa)")
                    .putString(KEY_MESSAGE, "${meeting.title}\nBắt đầu: ${meeting.startTime} | Địa điểm: ${meeting.location}")
                    .putInt(KEY_MEETING_ID, meeting.id)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<MeetingReminderWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(false)
                            .build()
                    )
                    .build()

                val workName = "meeting_reminder_work_${meeting.id}"
                WorkManager.getInstance(context).enqueueUniqueWork(
                    workName,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun cancelMeetingReminder(context: Context, meetingId: Int) {
            val workName = "meeting_reminder_work_$meetingId"
            WorkManager.getInstance(context).cancelUniqueWork(workName)
        }
    }
}
