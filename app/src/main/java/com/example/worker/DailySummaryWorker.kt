package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.R
import com.example.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DailySummaryWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val db = AppDatabase.getInstance(context)
            val todayMeetings = db.meetingDao().getMeetingsByDateSync(todayStr)

            val displayDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            val title = "Lịch công tác ngày $displayDate - UBND xã"
            val message = if (todayMeetings.isEmpty()) {
                "Hôm nay bạn không có cuộc họp nào được xếp lịch."
            } else {
                val count = todayMeetings.size
                val details = todayMeetings.take(3).joinToString("; ") { "${it.startTime}: ${it.title}" }
                "Hôm nay có $count cuộc họp: $details"
            }

            showSummaryNotification(context, title, message)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun showSummaryNotification(context: Context, title: String, message: String) {
        val channelId = "vhxh_daily_summary_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tóm tắt Lịch công tác Hàng ngày",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo tóm tắt danh sách cuộc họp vào đầu ngày làm việc"
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

        notificationManager.notify(1001, notification)
    }

    companion object {
        fun scheduleDailySummaryWorker(context: Context) {
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 7)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
            }

            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "VhxhDailySummaryWork",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        }
    }
}
