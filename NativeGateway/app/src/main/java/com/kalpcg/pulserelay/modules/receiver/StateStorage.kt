package com.kalpcg.pulserelay.modules.receiver

import com.kalpcg.pulserelay.modules.settings.KeyValueStorage
import com.kalpcg.pulserelay.modules.settings.get

class StateStorage(
    private val storage: KeyValueStorage,
) {
    var mmsLastProcessedID: Long
        get() = storage.get<Long>(MMS_LAST_PROCESSED_ID) ?: 0
        set(value) = storage.set(MMS_LAST_PROCESSED_ID, value)

    companion object {
        private val PREFIX = "state."

        private val MMS_LAST_PROCESSED_ID = PREFIX + "last_processed_id"
    }
}