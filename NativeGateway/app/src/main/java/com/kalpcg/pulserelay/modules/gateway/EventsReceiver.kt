package com.kalpcg.pulserelay.modules.gateway

import android.util.Log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.kalpcg.pulserelay.domain.EntitySource
import com.kalpcg.pulserelay.modules.events.EventBus
import com.kalpcg.pulserelay.modules.events.EventsReceiver
import com.kalpcg.pulserelay.modules.gateway.events.DeviceRegisteredEvent
import com.kalpcg.pulserelay.modules.gateway.events.MessageEnqueuedEvent
import com.kalpcg.pulserelay.modules.gateway.events.SettingsUpdatedEvent
import com.kalpcg.pulserelay.modules.gateway.events.WebhooksUpdatedEvent
import com.kalpcg.pulserelay.modules.gateway.services.SSEForegroundService
import com.kalpcg.pulserelay.modules.gateway.workers.PullMessagesWorker
import com.kalpcg.pulserelay.modules.gateway.workers.SendStateWorker
import com.kalpcg.pulserelay.modules.gateway.workers.SettingsUpdateWorker
import com.kalpcg.pulserelay.modules.gateway.workers.WebhooksUpdateWorker
import com.kalpcg.pulserelay.modules.messages.events.MessageStateChangedEvent
import com.kalpcg.pulserelay.modules.ping.events.PingEvent
import org.koin.core.component.get

class EventsReceiver : EventsReceiver() {

    private val settings = get<GatewaySettings>()

    override suspend fun collect(eventBus: EventBus) {
        coroutineScope {
            launch {
                Log.d("EventsReceiver", "launched MessageEnqueuedEvent")
                eventBus.collect<MessageEnqueuedEvent> { event ->
                    Log.d("EventsReceiver", "Event: $event")

                    if (!settings.enabled) return@collect

                    PullMessagesWorker.start(get())
                }
            }
            launch {
                Log.d("EventsReceiver", "launched MessageStateChangedEvent")
                val allowedSources = setOf(EntitySource.Cloud, EntitySource.Gateway)
                eventBus.collect<MessageStateChangedEvent> { event ->
                    Log.d("EventsReceiver", "Event: $event")

                    if (!settings.enabled) return@collect

                    if (event.source !in allowedSources) return@collect

                    SendStateWorker.start(get(), event.id)
                }
            }

            launch {
                Log.d("EventsReceiver", "launched PingEvent")
                eventBus.collect<PingEvent> {
                    Log.d("EventsReceiver", "Event: $it")

                    if (!settings.enabled) return@collect

                    PullMessagesWorker.start(get())
                }
            }

            launch {
                Log.d("EventsReceiver", "launched WebhooksUpdatedEvent")
                eventBus.collect<WebhooksUpdatedEvent> {
                    Log.d("EventsReceiver", "Event: $it")

                    if (!settings.enabled) return@collect

                    WebhooksUpdateWorker.start(get())
                }
            }

            launch {
                Log.d("EventsReceiver", "launched SettingsUpdatedEvent")
                eventBus.collect<SettingsUpdatedEvent> {
                    Log.d("EventsReceiver", "Event: $it")

                    if (!settings.enabled) return@collect

                    SettingsUpdateWorker.start(get())
                }
            }

            launch {
                Log.d("EventsReceiver", "launched DeviceRegisteredEvent")
                eventBus.collect<DeviceRegisteredEvent> {
                    Log.d("EventsReceiver", "Event: $it")

                    if (!settings.enabled) return@collect
                    if (settings.fcmToken != null) return@collect

                    SSEForegroundService.start(get())
                }
            }
        }

    }
}