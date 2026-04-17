package com.kalpcg.pulserelay.modules.events

data class ExternalEvent(
    val type: ExternalEventType,
    val data: String?,
)
