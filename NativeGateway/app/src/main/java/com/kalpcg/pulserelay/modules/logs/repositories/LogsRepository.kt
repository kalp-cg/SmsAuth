package com.kalpcg.pulserelay.modules.logs.repositories

import androidx.lifecycle.distinctUntilChanged
import com.kalpcg.pulserelay.modules.logs.db.LogEntriesDao

class LogsRepository(
    private val dao: LogEntriesDao
) {
    val lastEntries = dao.selectLast().distinctUntilChanged()
}