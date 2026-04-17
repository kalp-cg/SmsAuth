package com.kalpcg.pulserelay.domain

enum class EntitySource {
    Local,
    Cloud,

    @Deprecated("Not used anymore")
    Gateway,
}