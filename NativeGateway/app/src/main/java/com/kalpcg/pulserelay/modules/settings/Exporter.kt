package com.kalpcg.pulserelay.modules.settings

interface Exporter {
    fun export(): Map<String, *>
}