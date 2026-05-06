package com.johnreicabunas.clockwise.platform

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.johnreicabunas.clockwise.MainActivity
import com.johnreicabunas.clockwise.domain.model.RepeatFrequency
import com.johnreicabunas.clockwise.domain.model.ScheduledItem
import com.johnreicabunas.clockwise.domain.time.nextOccurrenceAfter
import com.johnreicabunas.clockwise.domain.time.notificationBody
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class ScheduleAlarmReceiver : BroadcastReceiver() {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Clockwise reminder" }
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val itemJson = intent.getStringExtra(EXTRA_ITEM_JSON)

        showNotification(context, itemId, title, body)

        val item = itemJson
            ?.let { runCatching { json.decodeFromString<ScheduledItem>(it) }.getOrNull() }
            ?: return

        if (item.repeatRule.frequency != RepeatFrequency.NONE) {
            val next = nextOccurrenceAfter(item, Clock.System.now() + 1.minutes) ?: return
            val nextItem = item.copy(resolvedInstant = next.toString())
            runBlocking {
                AndroidAlertScheduler(context).schedule(nextItem, nextItem.notificationBody())
            }
        }
    }

    private fun showNotification(
        context: Context,
        itemId: String,
        title: String,
        body: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val launchIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            itemId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body.lineSequence().firstOrNull().orEmpty())
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(itemId.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "clockwise_scheduled_items"
        const val EXTRA_ITEM_ID = "item_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_ITEM_JSON = "item_json"
    }
}
