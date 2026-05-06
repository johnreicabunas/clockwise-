package com.johnreicabunas.clockwise.platform

import com.johnreicabunas.clockwise.domain.model.ScheduledItem

class NoOpAlertScheduler : AlertScheduler {
    override suspend fun schedule(item: ScheduledItem, body: String) = Unit
    override suspend fun cancel(itemId: String) = Unit
}
