package com.kalpcg.pulserelay.modules.webhooks.domain

import com.kalpcg.pulserelay.domain.EntitySource

data class WebHookDTO(
    val id: String?,
    val deviceId: String?,
    val url: String,
    val event: WebHookEvent,
    val source: EntitySource,
)
