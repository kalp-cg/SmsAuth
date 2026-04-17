package com.kalpcg.pulserelay.modules.messages.events

import com.kalpcg.pulserelay.domain.EntitySource
import com.kalpcg.pulserelay.domain.ProcessingState
import com.kalpcg.pulserelay.modules.events.AppEvent

class MessageStateChangedEvent(
    val id: String,
    val source: EntitySource,
    val phoneNumbers: Set<String>,
    val state: ProcessingState,
    val simNumber: Int?,
    val partsCount: Int?,
    val error: String?
): AppEvent(NAME) {

    companion object {
        const val NAME = "MessageStateChangedEvent"
    }
}