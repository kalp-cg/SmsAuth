package com.kalpcg.pulserelay.modules.gateway.events

import com.kalpcg.pulserelay.modules.events.AppEvent

class WebhooksUpdatedEvent : AppEvent(NAME) {
    companion object {
        const val NAME = "WebhooksUpdatedEvent"
    }
}