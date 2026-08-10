package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.Meeting
import com.example.receiver.ReminderBroadcastReceiver
import com.example.worker.MeetingReminderWorker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReminderScheduler {

    fun scheduleMeetingReminder(context: Context, meeting: Meeting) {
        // Enqueue WorkManager task
        MeetingReminderWorker.scheduleMeetingReminder(context, meeting)

        if (meeting.reminderMinutes <= 0 || meeting.isCompleted) return

        val dateTimeStr = "${meeting.date} ${meeting.startTime}"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        try {
            val meetingTime = sdf.parse(dateTimeStr) ?: return
            val calendar = Calendar.getInstance().apply {
                time = meetingTime
                add(Calendar.MINUTE, -meeting.reminderMinutes)
            }

            val triggerMillis = calendar.timeInMillis
            if (triggerMillis <= System.currentTimeMillis()) return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val reminderMsg = if (meeting.reminderMinutes >= 60) {
                "${meeting.reminderMinutes / 60} giờ"
            } else {
                "${meeting.reminderMinutes} phút"
            }
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("EXTRA_TITLE", "⏰ Nhắc lịch họp (Còn $reminderMsg)")
                putExtra("EXTRA_MESSAGE", "${meeting.title} - Bắt đầu lúc ${meeting.startTime} tại ${meeting.location}")
                putExtra("EXTRA_MEETING_ID", meeting.id)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                meeting.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMillis, pendingIntent)
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelMeetingReminder(context: Context, meetingId: Int) {
        MeetingReminderWorker.cancelMeetingReminder(context, meetingId)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            meetingId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
