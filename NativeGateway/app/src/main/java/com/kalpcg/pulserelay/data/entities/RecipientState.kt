package com.kalpcg.pulserelay.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["messageId", "phoneNumber", "state"],
    foreignKeys = [
        ForeignKey(
            entity = MessageRecipient::class,
            parentColumns = ["messageId", "phoneNumber"],
            childColumns = ["messageId", "phoneNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipientState(
    val messageId: String,
    val phoneNumber: String,
    val state: com.kalpcg.pulserelay.domain.ProcessingState,
    val updatedAt: Long
)
