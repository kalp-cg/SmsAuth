package com.kalpcg.pulserelay.modules.gateway.events

import com.kalpcg.pulserelay.modules.events.AppEvent

class SettingsUpdatedEvent : AppEvent(NAME) {

    companion object {
        const val NAME = "SettingsUpdatedEvent"
    }
}