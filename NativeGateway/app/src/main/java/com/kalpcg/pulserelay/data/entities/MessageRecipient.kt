package com.kalpcg.pulserelay.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["messageId", "phoneNumber"],
    foreignKeys = [
        ForeignKey(entity = Message::class, parentColumns = ["id"], childColumns = ["messageId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class MessageRecipient(
    val messageId: String,
    val phoneNumber: String,
    val state: com.kalpcg.pulserelay.domain.ProcessingState = com.kalpcg.pulserelay.domain.ProcessingState.Pending,
    val error: String? = null
)
