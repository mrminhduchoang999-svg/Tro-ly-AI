package com.example.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.model.Meeting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MeetingWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MeetingWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            updateWidgets(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.receiver.ACTION_REFRESH_WIDGET"

        fun updateAppWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MeetingWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, MeetingWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH_WIDGET
                }
                context.sendBroadcast(intent)
            }
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                val upcomingMeetings = db.meetingDao().getUpcomingMeetingsSync()
                    .sortedWith(compareBy({ it.date }, { it.startTime }))
                    .take(3)

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_meetings)

                    // Intent to launch MainActivity when clicking widget root
                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val mainPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

                    // Refresh Button intent
                    val refreshIntent = Intent(context, MeetingWidgetProvider::class.java).apply {
                        action = ACTION_REFRESH_WIDGET
                    }
                    val refreshPendingIntent = PendingIntent.getBroadcast(
                        context,
                        1,
                        refreshIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.btn_widget_refresh, refreshPendingIntent)

                    if (upcomingMeetings.isEmpty()) {
                        views.setViewVisibility(R.id.layout_empty_widget, View.VISIBLE)
                        views.setViewVisibility(R.id.layout_meetings_container, View.GONE)
                    } else {
                        views.setViewVisibility(R.id.layout_empty_widget, View.GONE)
                        views.setViewVisibility(R.id.layout_meetings_container, View.VISIBLE)

                        bindMeetingItem(views, upcomingMeetings.getOrNull(0), R.id.layout_meeting_1, R.id.tv_title_1, R.id.tv_time_1, R.id.tv_location_1, R.id.tv_badge_1)
                        bindMeetingItem(views, upcomingMeetings.getOrNull(1), R.id.layout_meeting_2, R.id.tv_title_2, R.id.tv_time_2, R.id.tv_location_2, R.id.tv_badge_2)
                        bindMeetingItem(views, upcomingMeetings.getOrNull(2), R.id.layout_meeting_3, R.id.tv_title_3, R.id.tv_time_3, R.id.tv_location_3, R.id.tv_badge_3)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        private fun bindMeetingItem(
            views: RemoteViews,
            meeting: Meeting?,
            containerId: Int,
            titleId: Int,
            timeId: Int,
            locationId: Int,
            badgeId: Int
        ) {
            if (meeting == null) {
                views.setViewVisibility(containerId, View.GONE)
            } else {
                views.setViewVisibility(containerId, View.VISIBLE)
                views.setTextViewText(titleId, meeting.title)

                val formattedDate = formatDateString(meeting.date)
                val timeStr = if (meeting.endTime.isNotBlank()) {
                    "⏰ ${meeting.startTime} - ${meeting.endTime} | $formattedDate"
                } else {
                    "⏰ ${meeting.startTime} | $formattedDate"
                }
                views.setTextViewText(timeId, timeStr)

                val locStr = if (meeting.location.isNotBlank()) "📍 ${meeting.location}" else "📍 UBND xã"
                views.setTextViewText(locationId, locStr)

                val (badgeText, badgeColor) = when (meeting.priority) {
                    1 -> Pair("Quan trọng", android.graphics.Color.parseColor("#EF4444"))
                    3 -> Pair("Chuẩn bị", android.graphics.Color.parseColor("#EAB308"))
                    else -> Pair("Thường", android.graphics.Color.parseColor("#38BDF8"))
                }
                views.setTextViewText(badgeId, badgeText)
                views.setTextColor(badgeId, badgeColor)
            }
        }

        private fun formatDateString(dateStr: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                if (date != null) outputFormat.format(date) else dateStr
            } catch (e: Exception) {
                dateStr
            }
        }
    }
}
