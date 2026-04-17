package com.kalpcg.pulserelay.modules.incoming.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import com.kalpcg.pulserelay.modules.incoming.db.IncomingMessage
import com.kalpcg.pulserelay.modules.incoming.db.IncomingMessageTotals
import com.kalpcg.pulserelay.modules.incoming.db.IncomingMessagesDao

class IncomingMessagesRepository(private val dao: IncomingMessagesDao) {
    fun selectLast(limit: Int): LiveData<List<IncomingMessage>> =
        dao.selectLast(limit).distinctUntilChanged()

    val totals: LiveData<IncomingMessageTotals> = dao.getStats().distinctUntilChanged()

    fun insert(message: IncomingMessage) = dao.insert(message)
}
