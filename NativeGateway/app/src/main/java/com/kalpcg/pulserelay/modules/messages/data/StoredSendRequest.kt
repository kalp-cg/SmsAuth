package com.kalpcg.pulserelay.modules.messages.data

import com.kalpcg.pulserelay.data.entities.MessageRecipient
import com.kalpcg.pulserelay.domain.EntitySource
import com.kalpcg.pulserelay.domain.ProcessingState

class StoredSendRequest(
    val id: Long,
    val state: ProcessingState,
    val recipients: List<MessageRecipient>,
    source: EntitySource,
    message: Message,
    params: SendParams
) :
    SendRequest(source, message, params)