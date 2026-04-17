package com.kalpcg.pulserelay.providers

interface IPProvider {
    suspend fun getIP(): String?
}