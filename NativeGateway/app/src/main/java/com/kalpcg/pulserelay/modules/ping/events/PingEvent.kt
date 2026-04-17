package com.kalpcg.pulserelay.modules.ping.events

import com.kalpcg.pulserelay.domain.HealthResponse
import com.kalpcg.pulserelay.modules.events.AppEvent

class PingEvent(
    val health: HealthResponse,
) : AppEvent(TYPE) {
    companion object {
        const val TYPE = "PingEvent"
    }
}