package com.johnreicabunas.clockwise.platform

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.johnreicabunas.clockwise.MainActivity
import com.johnreicabunas.clockwise.R
import com.johnreicabunas.clockwise.domain.model.ScheduledItem
import com.johnreicabunas.clockwise.domain.model.ScheduledItemType
import com.johnreicabunas.clockwise.presentation.home.formatTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

class ClockwiseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, buildViews(context))
        }
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Re-renders all instances of the widget; call after schedules or Pro state change. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, ClockwiseWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                ids.forEach { manager.updateAppWidget(it, buildViews(context)) }
            }
        }

        private fun buildViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_clockwise)

            val launchIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, launchIntent)

            val isPro = context
                .getSharedPreferences("clockwise_billing", Context.MODE_PRIVATE)
                .getBoolean("pro_unlocked", false)

            views.setTextViewText(
                R.id.widget_next_schedule,
                if (isPro) nextScheduleLine(context) else "Unlock Clockwise Pro to see your next alert here"
            )
            return views
        }

        private fun nextScheduleLine(context: Context): String {
            val raw = context
                .getSharedPreferences("clockwise_schedules", Context.MODE_PRIVATE)
                .getString("scheduled_items", null)
                ?: return "No upcoming alerts"
            val items = runCatching {
                json.decodeFromString<List<ScheduledItem>>(raw)
            }.getOrNull() ?: return "No upcoming alerts"

            val now = Clock.System.now()
            val next = items.asSequence()
                .filter { it.enabled && it.deletedAt == null }
                .mapNotNull { item ->
                    runCatching { Instant.parse(item.resolvedInstant) }.getOrNull()
                        ?.takeIf { it > now }
                        ?.let { item to it }
                }
                .minByOrNull { it.second }
                ?: return "No upcoming alerts"

            val (item, instant) = next
            val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val kind = if (item.type == ScheduledItemType.ALARM) "Alarm" else "Meeting"
            return "$kind • ${item.title} • ${local.formatTime()}"
        }
    }
}
