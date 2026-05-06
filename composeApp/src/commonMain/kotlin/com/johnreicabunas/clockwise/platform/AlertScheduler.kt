package com.johnreicabunas.clockwise.platform

import com.johnreicabunas.clockwise.domain.model.ScheduledItem

interface AlertScheduler {
    suspend fun schedule(item: ScheduledItem, body: String)
    suspend fun cancel(itemId: String)
}
