package com.kalpcg.pulserelay.modules.logs.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kalpcg.pulserelay.modules.logs.db.LogEntry
import com.kalpcg.pulserelay.modules.logs.repositories.LogsRepository

class LogsViewModel(
    logs: LogsRepository
) : ViewModel() {
    val lastEntries: LiveData<List<LogEntry>> = logs.lastEntries
}