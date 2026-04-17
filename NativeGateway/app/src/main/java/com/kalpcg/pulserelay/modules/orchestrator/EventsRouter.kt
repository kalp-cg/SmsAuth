package com.kalpcg.pulserelay.modules.orchestrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.kalpcg.pulserelay.modules.events.EventBus
import com.kalpcg.pulserelay.modules.events.ExternalEvent
import com.kalpcg.pulserelay.modules.events.ExternalEventType
import com.kalpcg.pulserelay.modules.gateway.events.MessageEnqueuedEvent
import com.kalpcg.pulserelay.modules.gateway.events.SettingsUpdatedEvent
import com.kalpcg.pulserelay.modules.gateway.events.WebhooksUpdatedEvent
import com.kalpcg.pulserelay.modules.receiver.events.MessagesExportRequestedEvent

class EventsRouter(
    private val eventBus: EventBus
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun route(event: ExternalEvent) {
        scope.launch {
            when (event.type) {
                ExternalEventType.MessageEnqueued ->
                    eventBus.emit(
                        MessageEnqueuedEvent()
                    )

                ExternalEventType.WebhooksUpdated ->
                    eventBus.emit(
                        WebhooksUpdatedEvent()
                    )

                ExternalEventType.MessagesExportRequested ->
                    eventBus.emit(
                        MessagesExportRequestedEvent.withPayload(
                            requireNotNull(event.data)
                        )
                    )

                ExternalEventType.SettingsUpdated ->
                    eventBus.emit(
                        SettingsUpdatedEvent()
                    )
            }
        }
    }
}