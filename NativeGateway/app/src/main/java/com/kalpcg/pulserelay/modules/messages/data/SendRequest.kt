package com.kalpcg.pulserelay.modules.messages.data

import com.kalpcg.pulserelay.domain.EntitySource

open class SendRequest(
    val source: EntitySource,
    val message: Message,
    val params: SendParams,
)