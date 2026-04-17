package com.kalpcg.pulserelay.modules.webhooks.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kalpcg.pulserelay.domain.EntitySource
import com.kalpcg.pulserelay.modules.webhooks.domain.WebHookEvent

@Entity
data class WebHook(
    @PrimaryKey
    val id: String,
    val url: String,
    val event: WebHookEvent,
    val source: EntitySource,
)