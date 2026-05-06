package com.johnreicabunas.clockwise.platform

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.johnreicabunas.clockwise.domain.model.ScheduledItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

class AndroidAlertScheduler(
    private val context: Context
) : AlertScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val json = Json { encodeDefaults = true }

    override suspend fun schedule(item: ScheduledItem, body: String) {
        val triggerAt = runCatching { Instant.parse(item.resolvedInstant).toEpochMilliseconds() }
            .getOrNull()
            ?: return

        if (triggerAt <= Clock.System.now().toEpochMilliseconds()) {
            return
        }

        ensureChannel()
        val pendingIntent = pendingIntentFor(item.id, item.title, body, json.encodeToString(item))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    override suspend fun cancel(itemId: String) {
        alarmManager.cancel(pendingIntentFor(itemId, "", "", ""))
    }

    private fun pendingIntentFor(
        itemId: String,
        title: String,
        body: String,
        itemJson: String
    ): PendingIntent {
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            putExtra(ScheduleAlarmReceiver.EXTRA_ITEM_ID, itemId)
            putExtra(ScheduleAlarmReceiver.EXTRA_TITLE, title)
            putExtra(ScheduleAlarmReceiver.EXTRA_BODY, body)
            putExtra(ScheduleAlarmReceiver.EXTRA_ITEM_JSON, itemJson)
        }

        return PendingIntent.getBroadcast(
            context,
            itemId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            ScheduleAlarmReceiver.CHANNEL_ID,
            "Clockwise reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Timezone-aligned alarms and meeting reminders"
        }
        manager.createNotificationChannel(channel)
    }
}
