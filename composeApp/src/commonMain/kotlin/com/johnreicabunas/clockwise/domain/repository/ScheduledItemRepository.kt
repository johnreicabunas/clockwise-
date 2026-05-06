package com.johnreicabunas.clockwise.domain.repository

import com.johnreicabunas.clockwise.domain.model.ScheduledItem
import kotlinx.coroutines.flow.StateFlow

interface ScheduledItemRepository {
    val items: StateFlow<List<ScheduledItem>>

    suspend fun load()
    suspend fun save(item: ScheduledItem)
    suspend fun delete(itemId: String)
}
