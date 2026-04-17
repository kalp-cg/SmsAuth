package com.kalpcg.pulserelay.modules.localserver.events

import com.kalpcg.pulserelay.modules.events.AppEvent

class IPReceivedEvent(
    val localIP: String?,
    val publicIP: String?,
): AppEvent(NAME) {
    companion object {
        const val NAME = "IPReceivedEvent"
    }
}