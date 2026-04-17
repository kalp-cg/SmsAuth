package com.kalpcg.pulserelay.modules.receiver

import android.util.Log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.kalpcg.pulserelay.modules.events.EventBus
import com.kalpcg.pulserelay.modules.events.EventsReceiver
import com.kalpcg.pulserelay.modules.receiver.events.MessagesExportRequestedEvent
import org.koin.core.component.get
import org.koin.core.component.inject

class EventsReceiver : EventsReceiver() {
    private val service by inject<ReceiverService>()

    override suspend fun collect(eventBus: EventBus) {
        coroutineScope {
            launch {
                Log.d("EventsReceiver", "launched MessagesExportRequestedEvent")
                eventBus.collect<MessagesExportRequestedEvent> { event ->
                    Log.d("EventsReceiver", "Event: $event")

                    service.export(
                        get(),
                        event.since to event.until,
                    )
                }
            }
        }
    }
}