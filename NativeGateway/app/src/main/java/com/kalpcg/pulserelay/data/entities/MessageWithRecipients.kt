package com.kalpcg.pulserelay.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

data class MessageWithRecipients(
    @Embedded val message: Message,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId",
    )
    val recipients: List<MessageRecipient>,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId",
    )
    val states: List<MessageState> = emptyList(),
    @ColumnInfo(name = "rowid")
    val rowId: Long = 0,
) {
    val state: com.kalpcg.pulserelay.domain.ProcessingState
        get() = when {
            recipients.any { it.state == com.kalpcg.pulserelay.domain.ProcessingState.Pending } -> com.kalpcg.pulserelay.domain.ProcessingState.Pending
            recipients.any { it.state == com.kalpcg.pulserelay.domain.ProcessingState.Processed } -> com.kalpcg.pulserelay.domain.ProcessingState.Processed

            recipients.all { it.state == com.kalpcg.pulserelay.domain.ProcessingState.Failed } -> com.kalpcg.pulserelay.domain.ProcessingState.Failed
            recipients.all { it.state == com.kalpcg.pulserelay.domain.ProcessingState.Delivered } -> com.kalpcg.pulserelay.domain.ProcessingState.Delivered
            else -> com.kalpcg.pulserelay.domain.ProcessingState.Sent
        }
}
