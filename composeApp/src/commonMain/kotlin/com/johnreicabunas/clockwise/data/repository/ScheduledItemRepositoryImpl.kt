package com.johnreicabunas.clockwise.data.repository

import com.johnreicabunas.clockwise.data.local.ScheduleStorage
import com.johnreicabunas.clockwise.domain.model.RepeatFrequency
import com.johnreicabunas.clockwise.domain.model.ScheduledItem
import com.johnreicabunas.clockwise.domain.model.SyncStatus
import com.johnreicabunas.clockwise.domain.repository.ScheduledItemRepository
import com.johnreicabunas.clockwise.domain.time.nextOccurrenceAfter
import com.johnreicabunas.clockwise.domain.time.notificationBody
import com.johnreicabunas.clockwise.platform.AlertScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class ScheduledItemRepositoryImpl(
    private val storage: ScheduleStorage,
    private val alertScheduler: AlertScheduler
) : ScheduledItemRepository {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val storedItems = mutableListOf<ScheduledItem>()
    private val _items = MutableStateFlow<List<ScheduledItem>>(emptyList())
    override val items: StateFlow<List<ScheduledItem>> = _items.asStateFlow()

    override suspend fun load() {
        val loaded = storage.loadSchedulesJson()
            ?.let { raw -> runCatching { json.decodeFromString<List<ScheduledItem>>(raw) }.getOrNull() }
            ?: emptyList()

        storedItems.clear()
        storedItems += loaded.map { it.refreshedForNow() }
        publish()
        rescheduleActiveItems()
    }

    override suspend fun save(item: ScheduledItem) {
        val refreshedItem = item.refreshedForNow()
        val index = storedItems.indexOfFirst { it.id == refreshedItem.id }
        if (index >= 0) {
            storedItems[index] = refreshedItem
        } else {
            storedItems += refreshedItem
        }
        persist()
        publish()
        alertScheduler.cancel(refreshedItem.id)
        if (refreshedItem.enabled && refreshedItem.deletedAt == null) {
            alertScheduler.schedule(refreshedItem, refreshedItem.notificationBody())
        }
    }

    override suspend fun delete(itemId: String) {
        val now = Clock.System.now().toString()
        val index = storedItems.indexOfFirst { it.id == itemId }
        if (index >= 0) {
            storedItems[index] = storedItems[index].copy(
                deletedAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.PENDING_DELETE
            )
        }
        persist()
        publish()
        alertScheduler.cancel(itemId)
    }

    private suspend fun persist() {
        storage.saveSchedulesJson(json.encodeToString(storedItems))
    }

    private fun publish() {
        _items.value = storedItems
            .filter { it.deletedAt == null }
            .sortedBy { it.resolvedInstant }
    }

    private suspend fun rescheduleActiveItems() {
        storedItems
            .filter { it.enabled && it.deletedAt == null }
            .forEach { item ->
                alertScheduler.cancel(item.id)
                alertScheduler.schedule(item, item.notificationBody())
            }
    }

    private fun ScheduledItem.refreshedForNow(): ScheduledItem {
        if (repeatRule.frequency == RepeatFrequency.NONE) {
            return this
        }
        val next = nextOccurrenceAfter(this, Clock.System.now()) ?: return this
        return copy(resolvedInstant = next.toString())
    }
}
