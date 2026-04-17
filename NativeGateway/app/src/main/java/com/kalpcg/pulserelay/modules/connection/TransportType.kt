package com.kalpcg.pulserelay.modules.connection

enum class TransportType(
    val value: Int
) {
    Unknown(1),
    Cellular(2),
    WiFi(4),
    Ethernet(8),
}