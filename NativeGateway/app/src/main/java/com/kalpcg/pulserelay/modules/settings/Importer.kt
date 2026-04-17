package com.kalpcg.pulserelay.modules.settings

interface Importer {
    fun import(data: Map<String, *>): Boolean
}