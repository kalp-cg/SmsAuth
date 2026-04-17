package com.kalpcg.pulserelay.modules.incoming.db

data class IncomingMessageTotals(
    val total: Long,
    val sms: Long,
    val dataSms: Long,
    val mms: Long,
)
