package com.kalpcg.pulserelay.modules.gateway.events

import com.kalpcg.pulserelay.modules.events.AppEvent

class MessageEnqueuedEvent : AppEvent(NAME) {
    companion object {
        const val NAME = "MessageEnqueuedEvent"
    }
}